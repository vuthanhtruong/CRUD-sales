import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AddressBookDTO, AddressBookService } from '../services/address-book.service';

@Component({ selector: 'app-addresses', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './addresses.component.html', styleUrls: ['./addresses.component.css'] })
export class AddressesComponent implements OnInit {
  addresses: AddressBookDTO[] = [];
  loading = false;
  editingId: string | null = null;
  form: AddressBookDTO = this.blank();

  constructor(private service: AddressBookService, private cdr: ChangeDetectorRef) {}
  ngOnInit() { this.load(); }
  blank(): AddressBookDTO { return { receiverName: '', receiverPhone: '', fullAddress: '', city: '', district: '', ward: '', label: 'Home', defaultAddress: false }; }
  load() { this.loading = true; this.service.getMine().subscribe({ next: (x) => { this.addresses = x; this.loading = false; this.cdr.detectChanges(); }, error: () => { this.loading = false; this.cdr.detectChanges(); } }); }
  edit(a: AddressBookDTO) { this.editingId = a.id || null; this.form = { ...a }; }
  reset() { this.editingId = null; this.form = this.blank(); }
  save() { const req = this.editingId ? this.service.update(this.editingId, this.form) : this.service.create(this.form); req.subscribe({ next: () => { this.reset(); this.load(); } }); }
  setDefault(a: AddressBookDTO) { if (!a.id) return; this.service.setDefault(a.id).subscribe({ next: () => this.load() }); }
  delete(a: AddressBookDTO) { if (!a.id) return; this.service.delete(a.id).subscribe({ next: () => this.load() }); }
}
