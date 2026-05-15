# JasperReports PDF Export Update

## What changed

The product PDF export now uses JasperReports on the Spring Boot backend.

The existing endpoint is unchanged:

```http
GET /api/products/export?format=pdf
```

Angular still downloads the returned `Blob` exactly as before. No frontend API change is required.

## Backend files added/changed

### Added

- `Backend/src/main/java/com/example/demo/service/JasperProductReportService.java`
  - Loads and compiles the Jasper template once.
  - Fills the report with product DTO data.
  - Exports the filled report to PDF bytes.

- `Backend/src/main/resources/reports/product-list.jrxml`
  - JasperReports template for the product list PDF.
  - Landscape A4 layout.
  - Includes title, total product count, generated timestamp, table headers, table rows, and page footer.

### Changed

- `Backend/pom.xml`
  - Removed the direct OpenPDF dependency.
  - Added JasperReports dependency:

```xml
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.21.3</version>
</dependency>
```

- `Backend/src/main/java/com/example/demo/service/ProductServiceImpl.java`
  - The `pdf` branch now delegates to `JasperProductReportService`.
  - Excel export still uses Apache POI.
  - Word export still uses Apache POI XWPF.

## Export flow

```text
Angular Product Export Modal
→ GET /api/products/export?format=pdf
→ ProductController.exportProducts(...)
→ ProductServiceImpl.exportProducts(...)
→ buildPdfExport(...)
→ JasperProductReportService.buildProductListPdf(...)
→ JasperReports fills product-list.jrxml
→ Backend returns application/pdf bytes
→ Angular downloads products_yyyyMMdd_HHmmss.pdf
```

## Build note

Run the backend build from `Backend/`:

```bash
./mvnw clean package -DskipTests
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## PDF export failure fix

The PDF branch was hardened for JasperReports runtime export:

- Added `net.sf.jasperreports:jasperreports-fonts:6.21.3` so Jasper can render/export Unicode text with bundled DejaVu font support.
- Reworked `Backend/src/main/resources/reports/product-list.jrxml` so styles are declared before parameters/fields and the report uses `DejaVu Sans` with `Identity-H` PDF encoding.
- Replaced deprecated overflow handling in text fields with `textAdjust="StretchHeight"`.
- Added backend logging/root-cause messages in `JasperProductReportService` so a future export failure exposes the real Jasper exception instead of only a generic frontend message.
- Updated the Angular export modal to display the backend error message when the response body is a JSON/Blob error.
- Made status parsing case-insensitive in `ProductServiceImpl`.

Run backend after dependencies refresh:

```bash
cd Backend
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

If you copied this project between Windows/Linux, reinstall frontend dependencies on the target OS before building Angular:

```bash
cd Frontend/untitled
npm install
npm run build
```
