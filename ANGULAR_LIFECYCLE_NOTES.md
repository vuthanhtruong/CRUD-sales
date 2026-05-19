# Angular lifecycle update

Đã bổ sung vòng đời Angular cho các component trong `Frontend/src/app` theo hướng an toàn, không đổi luồng nghiệp vụ hiện tại.

## Các thay đổi chính

- Thêm `AfterViewInit` để đồng bộ lại view sau lần render đầu tiên bằng `ChangeDetectorRef.detectChanges()`.
- Thêm `OnDestroy` để detach change detector khi component bị hủy.
- Thêm `DestroyRef` + `takeUntilDestroyed(this.destroyRef)` cho các Observable subscription trong component, giúp tự động cleanup khi Angular destroy component.
- Giữ nguyên `OnInit` hiện có để load dữ liệu ban đầu.

## Kiểm tra

Đã chạy `npm run build` trong thư mục `Frontend` thành công. Angular CLI chỉ báo warning về bundle budget ban đầu vượt mức cấu hình, không phải lỗi biên dịch.
