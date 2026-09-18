package io.virinchi.dhammanature.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Types of evidence a vendor must upload so administrators can judge legitimacy before approving. */
@Getter
@RequiredArgsConstructor
public enum VendorDocType {
    BUSINESS_REGISTRATION("Business registration"),
    TAX_ID("Tax identification / TIN"),
    SHOP_LOCATION("Shop location proof (lease, utility bill)"),
    IDENTITY("Government ID of the owner"),
    PAST_WORK("Past work / portfolio / references"),
    OTHER("Other supporting evidence");

    private final String displayName;
}