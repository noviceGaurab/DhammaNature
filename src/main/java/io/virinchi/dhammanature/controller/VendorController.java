package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.VendorDocument;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import io.virinchi.dhammanature.model.enums.VendorDocType;
import io.virinchi.dhammanature.service.MarketplaceService;
import io.virinchi.dhammanature.service.VendorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vendor studio - a seller dashboard plus a dedicated "add product" page
 * (product name, price, description, genre, category, stock, photo).
 * Verified vendors can list products; pending vendors see their application
 * status and a preview of the catalogue area.
 */
@Controller
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final MarketplaceService marketplaceService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/vendor/studio")
    public String studio(HttpSession session, Model model) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        vendorService.findByUser(user.getId()).ifPresent(vendor -> {
            var products = marketplaceService.productsForVendor(vendor.getId());
            model.addAttribute("vendor", vendor);
            model.addAttribute("myProducts", products);
            model.addAttribute("productCount", products.size());
            model.addAttribute("categoryCount",
                    products.stream().map(p -> p.getCategory()).collect(Collectors.toSet()).size());
            model.addAttribute("totalStock",
                    products.stream().mapToInt(p -> p.getStockQuantity()).sum());
            model.addAttribute("myDocuments", vendorService.documents(vendor.getId()));
            model.addAttribute("docTypes", VendorDocType.values());
        });
        return "vendor/studio";
    }

    @GetMapping("/vendor/studio/products/add")
    public String addProductPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        var vendorOpt = vendorService.findByUser(user.getId());
        if (vendorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Apply to become a vendor first.");
            return "redirect:/vendor/studio";
        }
        model.addAttribute("vendor", vendorOpt.get());
        model.addAttribute("categories", ProductCategory.values());
        return "vendor/product-add";
    }

    @PostMapping("/vendor/studio/apply")
    public String apply(@RequestParam String vendorName,
                        @RequestParam(required = false) String contactDetails,
                        @RequestParam(required = false) String address,
                        @RequestParam(required = false) List<MultipartFile> documents,
                        @RequestParam(required = false) List<String> docTypes,
                        @RequestParam(required = false) List<String> docNotes,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        List<VendorDocument> docs = buildDocuments(documents, docTypes, docNotes);
                        if (docs.isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Please upload at least one evidence document (business registration, tax ID, shop location or government ID).");
                        }
                        vendorService.applyAsVendor(user, vendorName, contactDetails, address, docs);
                        redirectAttributes.addFlashAttribute("success",
                                "Vendor application submitted with " + docs.size() + " document(s) - pending admin verification. Once verified, you can list products.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/vendor/studio";
                })
                .orElse("redirect:/login");
    }

    private List<VendorDocument> buildDocuments(List<MultipartFile> files, List<String> types, List<String> notes)
            throws IOException {
        List<VendorDocument> docs = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return docs;
        }
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            if (f == null || f.isEmpty()) {
                continue;
            }
            VendorDocType type = VendorDocType.OTHER;
            if (types != null && i < types.size() && types.get(i) != null && !types.get(i).isBlank()) {
                try {
                    type = VendorDocType.valueOf(types.get(i));
                } catch (IllegalArgumentException ignored) {
                    type = VendorDocType.OTHER;
                }
            }
            String note = (notes != null && i < notes.size()) ? notes.get(i) : null;
            docs.add(VendorDocument.builder()
                    .docType(type)
                    .originalName(f.getOriginalFilename())
                    .contentType(f.getContentType())
                    .note(note == null || note.isBlank() ? null : note.trim())
                    .data(f.getBytes())
                    .build());
        }
        return docs;
    }

    @PostMapping("/vendor/studio/products")
    public String addProduct(@RequestParam String productName,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String genre,
                             @RequestParam BigDecimal price,
                             @RequestParam(defaultValue = "1") int stockQuantity,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) ProductCategory category,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> vendorService.findByUser(user.getId())
                        .map(vendor -> {
                            try {
                                if (!vendor.isVerified()) {
                                    throw new IllegalStateException(
                                            "Your vendor account is still pending review - you can list products once an administrator approves you.");
                                }
                                if (price == null || price.signum() <= 0) {
                                    throw new IllegalArgumentException("Please enter a price greater than zero.");
                                }
                                if (productName == null || productName.isBlank()) {
                                    throw new IllegalArgumentException("Please give your product a name.");
                                }
                                if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                                    throw new IllegalArgumentException("Please choose an image file (JPG, PNG, GIF, etc.).");
                                }
                                marketplaceService.addProduct(vendor.getId(), productName.trim(),
                                        description, genre, price, stockQuantity, file.getBytes(), file.getContentType(), category);
                                redirectAttributes.addFlashAttribute("success",
                                        "\"" + productName.trim() + "\" is now listed on the marketplace.");
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                            }
                            return "redirect:/vendor/studio";
                        })
                        .orElseGet(() -> {
                            redirectAttributes.addFlashAttribute("error", "Apply to become a vendor first.");
                            return "redirect:/vendor/studio";
                        }))
                .orElse("redirect:/login");
    }

    @GetMapping("/vendor/studio/products/{id}/edit")
    public String editProductPage(@PathVariable Integer id, HttpSession session,
                                  Model model, RedirectAttributes redirectAttributes) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        var vendorOpt = vendorService.findByUser(user.getId());
        if (vendorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Apply to become a vendor first.");
            return "redirect:/vendor/studio";
        }
        try {
            model.addAttribute("vendor", vendorOpt.get());
            model.addAttribute("product", marketplaceService.findVendorProduct(vendorOpt.get().getId(), id));
            model.addAttribute("categories", ProductCategory.values());
            return "vendor/product-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/studio";
        }
    }

    @PostMapping("/vendor/studio/products/{id}")
    public String updateProduct(@PathVariable Integer id,
                                @RequestParam String productName,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String genre,
                                @RequestParam BigDecimal price,
                                @RequestParam(defaultValue = "1") int stockQuantity,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) ProductCategory category,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> vendorService.findByUser(user.getId())
                        .map(vendor -> {
                            try {
                                if (!vendor.isVerified()) {
                                    throw new IllegalStateException(
                                            "Your vendor account is still pending review - you can manage products once an administrator approves you.");
                                }
                                if (price == null || price.signum() <= 0) {
                                    throw new IllegalArgumentException("Please enter a price greater than zero.");
                                }
                                if (productName == null || productName.isBlank()) {
                                    throw new IllegalArgumentException("Please give your product a name.");
                                }
                                marketplaceService.updateProduct(vendor.getId(), id, productName.trim(),
                                        description, genre, price, stockQuantity, imageUrl, category);
                                redirectAttributes.addFlashAttribute("success",
                                        "Product updated - \"" + productName.trim() + "\".");
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                            }
                            return "redirect:/vendor/studio";
                        })
                        .orElseGet(() -> {
                            redirectAttributes.addFlashAttribute("error", "Apply to become a vendor first.");
                            return "redirect:/vendor/studio";
                        }))
                .orElse("redirect:/login");
    }

    @PostMapping("/vendor/studio/products/{id}/delete")
    public String deleteProduct(@PathVariable Integer id, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> vendorService.findByUser(user.getId())
                        .map(vendor -> {
                            try {
                                marketplaceService.deleteProduct(vendor.getId(), id);
                                redirectAttributes.addFlashAttribute("success", "Product removed from your catalogue.");
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                            }
                            return "redirect:/vendor/studio";
                        })
                        .orElseGet(() -> {
                            redirectAttributes.addFlashAttribute("error", "Apply to become a vendor first.");
                            return "redirect:/vendor/studio";
                        }))
                .orElse("redirect:/login");
    }
}