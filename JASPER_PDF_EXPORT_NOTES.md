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
