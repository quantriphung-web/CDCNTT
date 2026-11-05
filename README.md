# Baby & SafeHome AI Security System v2 (`baby_theft_demo_v2`)

Hệ thống **an ninh và trông trẻ thông minh đa ngữ cảnh** bằng thị giác máy tính. Toàn bộ phần AI chạy tại máy trạm cục bộ (Edge AI), kết hợp máy chủ Dart, cơ sở dữ liệu PostgreSQL và ứng dụng di động Flutter, hoạt động trong mạng Wi-Fi nội bộ, **không đẩy video lên Cloud**.

> Báo cáo môn Chuyên đề CNTT – Phân hiệu Trường Đại học Thủy Lợi
> Sinh viên: Phùng Trí Quân (MSSV 2351067109, lớp S26-65CNTT) · GVHD: ThS. Hoàng Văn Quý

---

## Tính năng chính

- **Tự động nhận diện ngữ cảnh (Smart Dynamic Scenario AI)**: không cần kéo chuột vẽ vùng, chỉ hiển thị vùng nguy hiểm liên quan, các vùng khác được ẩn hoàn toàn.
- **7 kịch bản an toàn**:

| Kịch bản | Video | Vùng / cơ chế |
|---|---|---|
| Bé chơi ở cầu thang | `babe_play_at_stair` | Vùng cầu thang (đỏ) |
| Bé ra cửa / ban công | `baby_go_outside` | Vùng cửa (cam) |
| Thú cưng chạy ra ngoài | `dog_run_outside` | Vùng lối thoát (lục lam) |
| Cháy bếp | `kitchen_on_fire` | Phát hiện lửa HSV + YCrCb |
| Người già té ngã | `old_women_fall` | Aspect Ratio + vận tốc rơi |
| Kẻ trộm đột nhập | `theft` | FaceID: không khớp người nhà |
| Gia đình sinh hoạt | `family` | FaceID: khớp ảnh trong `family/` |

- **Phát hiện và bám vết**: YOLOv8n + ByteTrack (Kalman Filter, ghép cặp hai giai đoạn).
- **Nhận diện khuôn mặt**: YuNet + SFace (vector 128 chiều, ngưỡng Cosine 0.363), bộ đệm bỏ phiếu theo Track ID.
- **Phát hiện khói/lửa** bằng camera trên hai không gian màu HSV và YCrCb.
- **Cảnh báo thời gian thực**: HTTP POST → Dart → PostgreSQL → WebSocket → Push Notification trên Android.
- **Ứng dụng Flutter** Dark Mode: quản lý và xóa cảnh báo, đăng ký khuôn mặt bằng camera điện thoại (quét kiểu ngân hàng).
- Chạy trên **CPU laptop phổ thông**, không cần GPU rời.

---

## Kiến trúc 4 tầng

```
Camera / Video
     │
     ▼
┌──────────────────────┐   HTTP POST /api/alerts    ┌─────────────────────┐   SQL   ┌────────────────┐
│ Trạm AI Edge (Python)│ ─────────────────────────► │ Backend Dart (Shelf)│ ──────► │ PostgreSQL 14+ │
│ YOLOv8n + ByteTrack  │                            │ REST + WebSocket    │         │ baby_security_db│
│ FaceID, khói/lửa, ngã│                            └──────────┬──────────┘         └────────────────┘
└──────────────────────┘                                       │ WebSocket /ws (NEW_ALERT)
                                                               ▼
                                                    ┌─────────────────────┐
                                                    │ Ứng dụng Flutter    │
                                                    │ Push, rung, còi hú  │
                                                    └─────────────────────┘
```

| Tầng | Công nghệ | Vai trò | Cổng / kênh |
|---|---|---|---|
| Trạm AI Edge | Python, YOLOv8n, ByteTrack, OpenCV | Nhận 7 tình huống, khói lửa, té ngã | Camera / video |
| FaceID | YuNet + SFace (ONNX) | Phân biệt người nhà và người lạ | Cosine ≥ 0.363 |
| Notifier | HTTP POST JSON + Base64 | Gửi cảnh báo, heartbeat | LAN `:8080` |
| Backend | Dart Shelf | REST API, WebSocket broadcast | `0.0.0.0:8080` |
| CSDL | PostgreSQL 14+ | Bảng `alerts`, `system_status` | `5432` |
| Mobile | Flutter (Android) | Giao diện, Push Notification | WebSocket `/ws` |

---

## Cấu trúc thư mục

```
baby_theft_demo_v2/
├── demo.py               # Vòng lặp AI chính: YOLOv8 + ByteTrack, 7 kịch bản, khói/lửa, té ngã
├── family_faces.py       # YuNet + SFace: phát hiện, căn chỉnh, so khớp khuôn mặt
├── notifier.py           # Gửi cảnh báo bất đồng bộ (thread nền, cooldown 5 giây)
├── enroll.py             # Đăng ký khuôn mặt bằng webcam máy tính
├── test_webcam_face.py   # Kiểm tra nhận diện mặt qua webcam
├── danger_zones.json     # Tọa độ vùng nguy hiểm mẫu
├── family/               # Ảnh người thân: family/<Tên>/*.jpg
├── models/               # yolov8n.pt, YuNet, SFace (.onnx)
├── video/                # 7 video kịch bản kiểm thử
├── alerts/               # Ảnh snapshot cảnh báo lưu cục bộ
├── backend/              # Máy chủ Dart Shelf + PostgreSQL
└── app/                  # Ứng dụng Flutter
```

---

## Yêu cầu môi trường

| Thành phần | Phiên bản đã thử nghiệm |
|---|---|
| Python | 3.12 (64-bit) |
| OpenCV | 4.10.0 (có `FaceDetectorYN`, `FaceRecognizerSF`) |
| Ultralytics | 8.2.0 |
| Dart SDK | 3.4.3 (Shelf 1.4.1, shelf_router 1.1.4, shelf_cors_headers 0.1.5, shelf_web_socket 3.0.0, postgres 3.2.1) |
| PostgreSQL | 14+ |
| Flutter | 3.22.0 (camera 0.11.0+2, flutter_local_notifications 17.2.4, permission_handler 11.4.0, web_socket_channel 3.0.1) |

Máy trạm thử nghiệm: Intel Core i7 gen 11, RAM 16GB, **không dùng GPU rời**. Điện thoại thử nghiệm: Xiaomi Redmi Note 11 Pro (Android 13/14).

---

## Cài đặt và chạy

> Các lệnh dưới đây là gợi ý theo cấu trúc dự án. Hãy điều chỉnh tên tệp và tham số cho khớp với mã nguồn của bạn.

### 1. Cơ sở dữ liệu PostgreSQL

Tạo database `baby_security_db`. Máy chủ Dart tạo bảng `alerts`, `system_status` và các chỉ mục khi khởi động (thông tin kết nối cấu hình trong `backend/`).

### 2. Backend Dart

```bash
cd backend
dart pub get
dart run bin/server.dart      # lắng nghe 0.0.0.0:8080
```

### 3. Trạm AI Edge

```bash
pip install ultralytics opencv-python numpy requests
python demo.py                # chạy kịch bản video / camera (xem tham số --conf, --imgsz)
```

Phím **M** chuyển chế độ phân tích `BOTH / BABY / THEFT`.

### 4. Đăng ký khuôn mặt người thân

- Qua máy tính: `python enroll.py` (SPACE chụp ảnh, Q hoàn tất).
- Qua điện thoại: mở màn hình đăng ký trong app, nhập tên, quét 5 ảnh. Ảnh được lưu vào `family/<Tên>/` trên máy tính.
- Kiểm tra nhanh: `python test_webcam_face.py`.

### 5. Ứng dụng Flutter

```bash
cd app
flutter pub get
flutter build apk --release   # APK ARM64 khoảng 7.4MB
```

Cài APK lên điện thoại (ADB hoặc tải trực tiếp), vào **Cài đặt** và nhập IP máy chủ, ví dụ `http://192.168.100.225:8080`. Máy tính và điện thoại phải cùng mạng Wi-Fi.

---

## REST API và WebSocket

| Phương thức | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/alerts` | Danh sách cảnh báo (`limit`, `offset`, `type`, `unread_only`) |
| POST | `/api/alerts` | Trạm AI gửi sự cố mới |
| DELETE | `/api/alerts/<id>` | Xóa một cảnh báo |
| DELETE | `/api/alerts` | Xóa toàn bộ lịch sử cảnh báo |
| POST | `/api/alerts/{id}/resolve` | Đánh dấu sự cố đã xử lý |
| POST | `/api/faces/register` | Nhận ảnh khuôn mặt (`name`, `image` base64) |
| GET | `/api/faces` | Danh sách thành viên và số ảnh |
| DELETE | `/api/faces/<name>` | Xóa dữ liệu khuôn mặt một người |
| GET / POST | `/api/status` | Đồng bộ trạng thái trông trẻ, an ninh, camera |
| WS | `/ws` | Sự kiện thời gian thực (`NEW_ALERT`, ...) |

### Bảng `alerts`

`id`, `alert_type` (`baby_danger`, `intruder`, `fire_smoke`, `elder_fall`, `pet_escape`), `title`, `message`, `severity` (`CRITICAL` / `WARNING` / `INFO`), `snapshot_url`, `is_read`, `is_resolved`, `track_id`, `created_at`.

---

## Thông số kỹ thuật quan trọng

| Tham số | Giá trị |
|---|---|
| Ngưỡng ByteTrack | `τ_high = 0.5`, `τ_low = 0.1`, mất vết tối đa 30 khung |
| Nhận diện mặt | `FACE_EVERY = 4`, `FAMILY_CONFIRM = 2`, ngưỡng Cosine 0.363 |
| Té ngã | `AR ≥ 1.20`, nằm yên ≥ 15 khung |
| Khói/lửa | Khung 160×120, diện tích contour > 150 px |
| Cooldown cảnh báo | 5.0 giây mỗi loại sự cố |
| Người lạ chưa xác minh | `UNVERIFIED_SEC = 8.0` giây |

---

## Kết quả thực nghiệm

- Độ chính xác trung bình trên 20 kịch bản: **97.2%**.
- Độ trễ toàn chu trình (sự kiện → điện thoại): trung bình **~103.5 ms**, luôn dưới 1 giây.
- Tải CPU **28–38%**, tổng RAM khoảng **745MB**, FPS ổn định **28–32** trên CPU.
- Băng thông ở trạng thái bình thường dưới 2KB/s.

Hạn chế đã ghi nhận: kịch bản cấu hình IP trong app cần cải thiện thêm, kịch bản backend tạm dừng cho thấy giới hạn của mô hình, độ chính xác nhóm té ngã (93.6%) và thú cưng (94.2%) thấp hơn các nhóm còn lại.

---

## Hướng phát triển

- Hỗ trợ nhiều camera RTSP/ONVIF (4–8 camera).
- Mã hóa TLS/HTTPS và WSS cho máy chủ Dart.
- Huấn luyện thêm mô hình cho camera hồng ngoại ban đêm.
- Thêm kênh cảnh báo Telegram Bot hoặc gọi thoại khẩn cấp.
- Đóng gói lên NVIDIA Jetson Nano / Raspberry Pi 5 (Edge AI Box dưới 15W).

---

## Tài liệu tham khảo

1. Ultralytics, *YOLOv8 Documentation*, https://docs.ultralytics.com/
2. Zhang et al., *ByteTrack: Multi-Object Tracking by Associating Every Detection Box*, ECCV 2022.
3. OpenCV Zoo, *YuNet* và *SFace*, https://github.com/opencv/opencv_zoo
4. Dart Shelf, https://pub.dev/packages/shelf
5. PostgreSQL 14 Documentation, https://www.postgresql.org/docs/
6. Flutter Documentation, https://docs.flutter.dev/
7. IETF, *RFC 6455: The WebSocket Protocol*, 2011.
