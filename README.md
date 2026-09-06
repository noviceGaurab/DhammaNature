# Dhamma Nature: A Way of Life System

Spring Boot 3.3 / Hibernate (JPA) / Thymeleaf / Lombok rewrite of the original
Eclipse Dynamic Web Project (Servlets + JSP + raw JDBC), targeting IntelliJ +
TiDB, matching the stack already proven in `SpringWebVir`.

## ⚠️ Before anything else: rotate your TiDB / mail credentials

The `SpringWebVir-main/src/main/resources/application.properties` you shared
had a real TiDB username/password and Gmail app password committed in plain
text. Please rotate both in the TiDB Cloud console and your Google account
settings — this project never hardcodes secrets (see below), but the old ones
should be considered exposed.

## Running it in IntelliJ

1. Open the project as a Maven project (IntelliJ will pick up `pom.xml`).
2. Set environment variables under **Run/Debug Configurations → Environment
   variables** (or a local, git-ignored `.env`):

   | Variable | Example |
   |---|---|
   | `DB_URL` | `jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/dhamma_nature?useSSL=true&serverTimezone=UTC` |
   | `DB_USERNAME` | your TiDB username |
   | `DB_PASSWORD` | your TiDB password |
   | `MAIL_USERNAME` / `MAIL_PASSWORD` | optional, only needed if you wire up real emails |
   | `ADMIN_EMAIL` / `ADMIN_PASSWORD` | optional, defaults to `admin@dhammanature.org` / `ChangeMe123!` — **change this in any real deployment** |

   `spring.jpa.hibernate.ddl-auto=update` will create/update all tables on
   TiDB automatically on first run — no manual schema needed.
3. Run `DhammaNatureApplication`. On first boot, `DataSeeder` creates the
   admin account and (if `app.seed-demo-data=true`, the default) a small set
   of demo centers/events/rewards/quiz so the site isn't empty.
4. Visit `http://localhost:8080`.

## What changed from the Eclipse project

- **Persistence**: raw JDBC/Servlets → Spring Data JPA repositories +
  Hibernate-managed entities with real `@ManyToOne`/`@OneToMany`/`@ManyToMany`
  relationships (see `src/main/java/io/virinchi/dhammanature/model`).
- **Views**: JSP (`webapp/*.jsp`) → Thymeleaf (`src/main/resources/templates`).
  Pages that were pure static/Lorem-ipsum markup (home, about, blog, contact,
  teachings, upcoming-sermons, community) were mechanically converted —
  same CSS classes and copy, just `.jsp` → `.html`, EL context-path
  expressions removed, and internal links repointed at the new routes.
  Pages that touched the database (login, signup, admin *, donations,
  comments/discussion, gallery) were rebuilt against the new service layer.
- **Auth**: plain-text password comparison → BCrypt hashing
  (`spring-security-crypto`, no full Spring Security filter chain pulled in).
- **New features**, all traceable to the market-analysis report's validated
  requirements (see class-level Javadoc comments referencing FR-xx / NFR-xx):
  meditation center directory + organization profiles, hybrid event booking,
  charity campaigns, reward points + redemption catalog, volunteer
  registration, a real marketplace (vendor verification, orders, wishlist,
  reviews), and a Dhamma quiz module.

## Old JSP → new route mapping

| Old file | New route |
|---|---|
| `home.jsp` | `/` |
| `login.jsp` | `/login` |
| `SignupPage.jsp` | `/signup` |
| `admin.jsp` | `/admin` |
| `admin_donations.jsp` | `/admin/donations` |
| `admin_comments.jsp` | `/admin/comments` |
| `admin_gallery.jsp` | `/admin/gallery` |
| `admin_page_interactions.jsp` | `/admin/interactions` |
| `community_about.jsp` | `/about` |
| `community_discuss.jsp`, `sermon1_discuss.jsp` | `/discuss` |
| `sermon1.jsp` | `/teachings` |
| `sermon1_descrpt.jsp` | `/teachings/detail` |
| `upcoming_sermons.jsp` | `/upcoming-sermons` |
| `upcoming_sermons_descrpt.jsp` | `/upcoming-sermons/detail` |
| `home_blog.jsp` | `/blog` |
| `home_blog_descrpt.jsp` | `/blog/detail` |
| `home_contact.jsp` | `/contact` |
| `home_donation.jsp` | `/donate` (now a real donation flow) |
| `donation_confirm.jsp` | `/donate/confirm` |
| `home_events.jsp`, `upcoming_events.jsp` (+ their `_descrpt` pages) | `/events`, `/events/{id}` (now a real, bookable events list) |
| `users_variety.jsp` | `/community` |

New areas with no old equivalent: `/centers`, `/centers/{id}`, `/rewards`,
`/volunteer`, `/charity`, `/marketplace`, `/marketplace/{id}`, `/quiz`,
`/quiz/{id}`, `/profile`, `/admin/centers`, `/admin/vendors`.

## Known limitation

This project was built and reviewed line-by-line for consistency (entity
fields ↔ repository query derivations ↔ service calls ↔ controller calls ↔
Thymeleaf model attributes), but it was **not compiled** in the sandbox this
was built in — that environment's network egress doesn't reach Maven Central,
only a fixed allowlist. Please run `mvn clean compile` (or just hit Run in
IntelliJ) as your first step; if anything surfaces, it should be minor.
