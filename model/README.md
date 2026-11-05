# Demo: Trông trẻ + Chống trộm (YOLOv8 + nhận diện mặt người nhà)

Dùng model **YOLOv8n pretrained** (tự tải lần đầu, không cần train) + ByteTrack có sẵn trong Ultralytics.
Chạy được trên laptop bằng CPU, đọc **file video** hoặc webcam.

## Cài đặt

```bash
pip install -r requirements.txt
```

## Chạy

```bash
python demo.py --source video.mp4          # demo bằng video
python demo.py --source video.mp4 --save ketqua.mp4   # vừa xem vừa lưu video kết quả
python demo.py --source 0                  # webcam
```

## Đăng ký người nhà (nhận diện mặt)

Tạo thư mục theo tên từng người trong `family/` và bỏ 3–5 ảnh rõ mặt vào:

```
family/
    Ba/   1.jpg 2.jpg 3.jpg
    Me/   1.jpg 2.jpg 3.jpg
```

Hoặc chụp bằng webcam: `python enroll.py --name Ba` (SPACE = chụp, Q = xong).
Lần đầu chạy chương trình tự tải 2 model nhận diện mặt (~40MB) vào thư mục `models/`.

Lần đầu mở video, chương trình hiện frame đầu tiên để bạn **kéo chuột chọn vùng giường / nơi trẻ nằm** (Enter để xác nhận, bỏ qua nếu không cần trông trẻ).
Vùng được lưu vào `zones_<tên video>.json` (dùng `--reselect` hoặc phím `R` để vẽ lại).

## Phím tắt

| Phím | Chức năng |
|------|-----------|
| M | Đổi chế độ: BOTH → BABY (trông trẻ) → THEFT (chống trộm) |
| R | Vẽ lại vùng giường |
| SPACE | Tạm dừng |
| S | Chụp ảnh màn hình |
| Q / ESC | Thoát |

## Chức năng

**Trông trẻ**
- Bé = người nằm trong vùng giường (nhỏ nhất), được khóa theo track ID.
- Đứng yên đủ `SLEEP_SEC` giây → "TRE DANG NGU"; cử động lại → cảnh báo "THUC DAY".
- Tâm bé ra ngoài vùng an toàn (giường thu nhỏ 15%) → "NGUY CO ROI KHOI GIUONG".

**Chống trộm (nhận diện người lạ, không cần vùng cấm)**
- Mỗi người (trừ bé) được theo dõi bằng track ID, cứ vài frame lại nhận diện mặt.
- Khớp mặt người nhà → khung **xanh** "NGUOI NHA: tên".
- Thấy mặt rõ nhưng không khớp ai → khung **đỏ** "NGUOI LA" + báo động, chụp ảnh vào `alerts/`, phát tiếng bíp.
- Xuất hiện hơn 8 giây mà chưa thấy được mặt (quay lưng, đội mũ, che mặt) → khung **cam** "CHUA XAC MINH" + cảnh báo. Tắt bằng `--no-unverified-alert`.
- Nhận nhầm người lạ thành người nhà → tăng `--face-thr 0.45`; không nhận ra người nhà → giảm (vd 0.30) và thêm ảnh đăng ký.

Các thông số (thời gian ngủ, ngưỡng chuyển động, số frame...) ở đầu file `demo.py`.
Thời gian ngủ mặc định 4 giây để demo; thực tế nên đặt khoảng 150 giây.

## Chọn video demo

Tìm video quay cố định (camera không rung), ví dụ trên Pexels / Pixabay / YouTube:
- "baby sleeping in crib" cho phần trông trẻ
- "person entering room door security camera" cho phần chống trộm

Có thể cắt ghép 2 đoạn thành 1 video để demo cả hai chế độ.

## Lưu ý

- YOLOv8n nhẹ nhưng có thể bỏ sót bé nằm sấp, nằm trong chăn hoặc quay từ trên cao. Nếu không phát hiện được:
  `python demo.py --source video.mp4 --model yolov8s.pt --conf 0.2`
- Phần "ngủ/thức" và "nguy cơ rơi" dựa trên luật đơn giản (độ dịch chuyển của bounding box), không phải model học riêng.
- Muốn thêm nhận diện mặt người nhà hoặc gửi Telegram thì cần mở rộng thêm.
