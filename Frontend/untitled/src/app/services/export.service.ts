import { Injectable } from '@angular/core';
import { ProductDTO } from './product.service';

@Injectable({ providedIn: 'root' })
export class ExportService {

  // =====================
  // EXCEL (.xlsx)
  // =====================
  async exportExcel(products: ProductDTO[], fileName = 'products'): Promise<void> {
    const XLSX = await import('xlsx');

    const rows = products.map((p, i) => ({
      'STT':          i + 1,
      'Product ID':   p.productId,
      'Product Name': p.productName,
      'Status':       p.status,
      'Type ID':      p.productTypeId,
      'Price':        p.price ?? 0,
      'Created By':   p.createdBy ?? '',
      'Description':  p.description ?? ''
    }));

    const ws = XLSX.utils.json_to_sheet(rows);

    // Column widths
    ws['!cols'] = [
      { wch: 5 }, { wch: 14 }, { wch: 28 }, { wch: 12 },
      { wch: 14 }, { wch: 12 }, { wch: 14 }, { wch: 36 }
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Products');
    XLSX.writeFile(wb, `${fileName}_${this.dateStamp()}.xlsx`);
  }

  // =====================
  // WORD (.docx)
  // =====================
  async exportWord(products: ProductDTO[], fileName = 'products'): Promise<void> {
    const { Document, Packer, Paragraph, Table, TableRow, TableCell,
            TextRun, HeadingLevel, AlignmentType, WidthType,
            BorderStyle } = await import('docx');
    const { saveAs } = await import('file-saver');

    const borderDef = {
      top:    { style: BorderStyle.SINGLE, size: 1, color: 'AAAAAA' },
      bottom: { style: BorderStyle.SINGLE, size: 1, color: 'AAAAAA' },
      left:   { style: BorderStyle.SINGLE, size: 1, color: 'AAAAAA' },
      right:  { style: BorderStyle.SINGLE, size: 1, color: 'AAAAAA' }
    };

    const cell = (text: string, bold = false, shading?: string) =>
      new TableCell({
        borders: borderDef,
        shading: shading ? { fill: shading } : undefined,
        children: [
          new Paragraph({
            alignment: AlignmentType.LEFT,
            children: [new TextRun({ text: String(text), bold, size: 20 })]
          })
        ]
      });

    const headers = ['#', 'Product ID', 'Product Name', 'Status', 'Type ID', 'Price', 'Created By', 'Description'];

    const headerRow = new TableRow({
      children: headers.map(h => cell(h, true, 'D6E4F7'))
    });

    const dataRows = products.map((p, i) =>
      new TableRow({
        children: [
          cell(String(i + 1)),
          cell(p.productId),
          cell(p.productName),
          cell(p.status),
          cell(p.productTypeId),
          cell(p.price != null ? p.price.toLocaleString('vi-VN') + ' ₫' : ''),
          cell(p.createdBy ?? ''),
          cell(p.description ?? '')
        ]
      })
    );

    const table = new Table({
      width: { size: 100, type: WidthType.PERCENTAGE },
      rows: [headerRow, ...dataRows]
    });

    const doc = new Document({
      sections: [{
        children: [
          new Paragraph({
            text: 'Product List',
            heading: HeadingLevel.HEADING_1,
            alignment: AlignmentType.CENTER,
            spacing: { after: 200 }
          }),
          new Paragraph({
            alignment: AlignmentType.RIGHT,
            spacing: { after: 300 },
            children: [
              new TextRun({
                text: `Exported: ${new Date().toLocaleString('vi-VN')}`,
                size: 18, color: '888888', italics: true
              })
            ]
          }),
          table,
          new Paragraph({
            spacing: { before: 400 },
            alignment: AlignmentType.RIGHT,
            children: [
              new TextRun({
                text: `Total: ${products.length} product(s)`,
                size: 20, bold: true
              })
            ]
          })
        ]
      }]
    });

    const blob = await Packer.toBlob(doc);
    saveAs(blob, `${fileName}_${this.dateStamp()}.docx`);
  }

  // =====================
  // PDF
  // =====================
  async exportPdf(products: ProductDTO[], fileName = 'products'): Promise<void> {
    const { default: jsPDF } = await import('jspdf');
    await import('jspdf-autotable');

    const doc = new (jsPDF as any)({ orientation: 'landscape', unit: 'mm', format: 'a4' });

    // Title
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text('Product List', doc.internal.pageSize.getWidth() / 2, 16, { align: 'center' });

    // Subtitle
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(130, 130, 130);
    doc.text(
      `Exported: ${new Date().toLocaleString('vi-VN')}   |   Total: ${products.length} product(s)`,
      doc.internal.pageSize.getWidth() / 2, 22, { align: 'center' }
    );
    doc.setTextColor(0, 0, 0);

    const head = [['#', 'Product ID', 'Product Name', 'Status', 'Type ID', 'Price', 'Created By', 'Description']];

    const body = products.map((p, i) => [
      i + 1,
      p.productId,
      p.productName,
      p.status,
      p.productTypeId,
      p.price != null ? p.price.toLocaleString('vi-VN') + ' d' : '',
      p.createdBy ?? '',
      p.description ?? ''
    ]);

    (doc as any).autoTable({
      head,
      body,
      startY: 27,
      styles:        { fontSize: 8, cellPadding: 2, overflow: 'linebreak' },
      headStyles:    { fillColor: [41, 98, 175], textColor: 255, fontStyle: 'bold' },
      alternateRowStyles: { fillColor: [240, 245, 255] },
      columnStyles: {
        0: { cellWidth: 8 },
        1: { cellWidth: 22 },
        2: { cellWidth: 40 },
        3: { cellWidth: 20 },
        4: { cellWidth: 20 },
        5: { cellWidth: 24 },
        6: { cellWidth: 22 },
        7: { cellWidth: 'auto' }
      },
      didDrawPage: (data: any) => {
        const pageCount = (doc as any).internal.getNumberOfPages();
        doc.setFontSize(8);
        doc.setTextColor(130);
        doc.text(
          `Page ${data.pageNumber} / ${pageCount}`,
          doc.internal.pageSize.getWidth() - 20,
          doc.internal.pageSize.getHeight() - 8
        );
        doc.setTextColor(0);
      }
    });

    doc.save(`${fileName}_${this.dateStamp()}.pdf`);
  }

  // =====================
  // HELPER
  // =====================
  private dateStamp(): string {
    const d = new Date();
    return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
  }
}