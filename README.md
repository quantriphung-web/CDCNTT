# Hệ Thống Điều Khiển Nhà Thông Minh Bằng Giọng Nói (ESP32, Vosk, MQTT)

## 📝 Tổng Quan

Đây là hệ thống điều khiển nhà thông minh bằng **lệnh giọng nói offline** — cho phép bật/tắt đèn, quạt, động cơ mà **không cần kết nối internet** để xử lý giọng nói. Hệ thống dùng vi điều khiển **ESP32**, mô hình nhận diện giọng nói **Vosk**, và giao thức **MQTT** để giao tiếp giữa laptop và ESP32 qua mạng Wi-Fi nội bộ. Phù hợp cho người mới tìm hiểu IoT, phát triển nhà thông minh, hoặc muốn làm hệ thống điều khiển bằng giọng nói không phụ thuộc cloud.

## 🎯 Mục Tiêu Dự Án

Xây dựng 1 hệ thống điều khiển nhà thông minh bằng giọng nói **dễ triển khai, hoạt động offline**. Lệnh giọng nói được thu và xử lý ngay trên laptop bằng model Vosk (không gửi dữ liệu ra ngoài), sau đó gửi xuống ESP32 qua MQTT để điều khiển thiết bị thật.

**Tính năng chính:**
- **Nhận diện giọng nói offline** — dùng model Vosk đã train sẵn, độ chính xác cao, không cần internet.
- **Giao tiếp MQTT nội bộ** — gửi lệnh tới ESP32 qua cùng mạng Wi-Fi, độ trễ thấp.
- **Điều khiển linh hoạt** — hỗ trợ nhiều thiết bị (đèn, quạt, động cơ bước), dễ thêm thiết bị mới.
- **Dễ triển khai** — có hướng dẫn chi tiết, phù hợp cả người mới lẫn người đã có kinh nghiệm.

## 🏗️ Kiến Trúc & Luồng Hoạt Động

Hệ thống gồm 2 phần chạy song song, giao tiếp qua MQTT:

```
[Micro] → [Laptop: Vosk nhận diện giọng nói] → [MQTT Broker] → [ESP32: điều khiển relay] → [Đèn/Quạt/Động cơ thật]
```

### 1. Nhận diện giọng nói (phía Laptop)

- Thu âm thanh trực tiếp từ micro.
- Dùng thư viện **Vosk** để chuyển giọng nói thành văn bản (Speech-to-Text).
- Python xử lý văn bản, so khớp với các từ khóa định trước (như "on", "off", "fan", "light"...).
- Nếu khớp đúng lệnh hợp lệ → gửi 1 tin nhắn MQTT tới topic tương ứng.

### 2. Giao tiếp MQTT

- Laptop đóng vai **MQTT Client (Publisher)** — gửi tin nhắn tới ESP32.
- ESP32 đóng vai **Subscriber** — đăng ký lắng nghe các topic liên quan (VD: `home/kitchen/light`, `home/fan`).

### 3. Điều khiển thiết bị (phía ESP32)

- Nhận tin nhắn MQTT và điều khiển thiết bị tương ứng.
- Dùng chân GPIO để điều khiển relay (đóng/ngắt điện cho đèn/động cơ), dễ dàng mở rộng thêm thiết bị.
- Hỗ trợ cả thiết bị đơn giản (đèn bật/tắt) lẫn động cơ bước (stepper motor) phức tạp hơn.

## 🤖 Về Phần AI — Vosk Hoạt Động Như Thế Nào?

**Điểm quan trọng cần hiểu rõ: dự án này KHÔNG tự train AI từ đầu.** Vosk là 1 model **đã được train sẵn (pretrained)** bởi nhóm phát triển Alpha Cephei, bạn chỉ **tải về và sử dụng**, không cần chuẩn bị dữ liệu hay huấn luyện lại.

**Cách Vosk nhận diện giọng nói bên trong (kiến trúc tổng quát của hệ thống ASR — Automatic Speech Recognition):**

1. **Acoustic Model (Mô hình âm học)** — 1 mạng neural (thường dựa trên kiến trúc Kaldi) đã học cách ánh xạ sóng âm thô (waveform) thành các đơn vị âm vị (phoneme) — được train sẵn trên hàng nghìn giờ dữ liệu giọng nói thật.
2. **Language Model (Mô hình ngôn ngữ)** — dự đoán chuỗi từ có khả năng xuất hiện hợp lý (dựa trên xác suất thống kê ngôn ngữ), giúp sửa lỗi nhận diện sai âm vị đơn lẻ.
3. **Decoder** — kết hợp cả 2 mô hình trên để tìm ra chuỗi văn bản có khả năng đúng nhất từ tín hiệu âm thanh đầu vào.

**Vì sao không cần tự train:**
- Việc train 1 model ASR từ đầu đòi hỏi hàng nghìn giờ dữ liệu giọng nói đã gán nhãn + tài nguyên tính toán rất lớn (GPU mạnh, nhiều ngày train).
- Vosk cung cấp sẵn nhiều model theo từng ngôn ngữ (tiếng Anh, tiếng Việt...) và kích thước khác nhau (nhỏ ~40MB cho thiết bị yếu, lớn hơn cho độ chính xác cao) — chỉ cần tải đúng model, không cần tự chuẩn bị dữ liệu.

**Nếu muốn "tùy biến" thêm AI cho hệ thống (không phải train lại từ đầu):**
- Có thể **fine-tune** (huấn luyện bổ sung) model Vosk có sẵn với giọng nói/từ vựng đặc thù của người dùng để tăng độ chính xác — đây là hướng phát triển nâng cao, khác với train từ đầu.
- Phần **so khớp từ khóa** (`if "light" in text`) hiện tại là logic thông thường, không phải AI — có thể nâng cấp thành model NLU (Natural Language Understanding) để hiểu linh hoạt hơn nhiều cách diễn đạt khác nhau của cùng 1 lệnh.

## 🛠️ Yêu Cầu Phần Cứng

- Vi điều khiển **ESP32**
- Micro (thu giọng nói)
- Laptop/PC (chạy nhận diện giọng nói + MQTT client)
- Module relay (điều khiển đèn/thiết bị khác)
- Động cơ bước + driver (tùy chọn)
- Router Wi-Fi (giao tiếp mạng nội bộ)

## 💻 Yêu Cầu Phần Mềm

- Python 3.x
- Thư viện Vosk (nhận diện giọng nói)
- PyAudio (thu âm thanh)
- paho-mqtt (giao tiếp MQTT)
- Arduino IDE (nạp firmware cho ESP32)

## 📋 Hướng Dẫn Cài Đặt

### Bước 1: Cài đặt Vosk để nhận diện giọng nói offline

```
pip install vosk pyaudio
```

Tải model Vosk tiếng Anh (hoặc tiếng Việt) tại [Vosk Models](https://alphacephei.com/vosk/models), giải nén, cập nhật đường dẫn `model_path` trong file Python.

### Bước 2: Cài đặt MQTT trên laptop

```
pip install paho-mqtt
```

Chạy file `Keyword_Spotting_speech_to_mqtt.ipynb` để bắt đầu lắng nghe lệnh giọng nói.

### Bước 3: Nạp code cho ESP32 bằng Arduino IDE

1. Mở file `esp32_mqtt_control.ino` bằng Arduino IDE.
2. Cập nhật tên Wi-Fi (SSID), mật khẩu, và địa chỉ IP của MQTT broker trong code.
3. Nạp code vào ESP32.

### Bước 4: Đấu nối ESP32 với thiết bị

1. **Đèn**: nối module relay để điều khiển thiết bị điện AC.
2. **Động cơ bước**: dùng module driver để điều khiển.
3. Đảm bảo tất cả linh kiện được cấp nguồn và nối đất (grounded) đúng cách.

## 🗣️ Ví Dụ Câu Lệnh

- "Turn on kitchen light" (Bật đèn nhà bếp)
- "Turn off bedroom light" (Tắt đèn phòng ngủ)
- "Turn on fan" (Bật quạt)
- "Turn off fan" (Tắt quạt)
- "Terminate" (Dừng chương trình lắng nghe)

## 📂 Các File Chính Trong Dự Án

- `Keyword_Spotting_speech_to_mqtt.ipynb` — Script Python xử lý nhận diện giọng nói và giao tiếp MQTT.
- `ESP32_mqtt_ArduinoIDE.ino` — Code Arduino nạp cho ESP32 để điều khiển thiết bị.

## 🚀 Hướng Phát Triển Trong Tương Lai

1. Hỗ trợ thêm nhiều loại thiết bị gia dụng khác.
2. Tích hợp thêm ứng dụng di động để điều khiển thủ công.
3. Triển khai tính năng nhận diện từ đánh thức (wake word) tùy chỉnh.
4. Mở rộng thêm nhiều lệnh giọng nói khác.
5. *(Đề xuất thêm)* Tích hợp Speaker Recognition (nhận diện ai đang nói) để phân quyền điều khiển theo từng người dùng.

---
*Tài liệu được dịch và bổ sung giải thích dựa trên dự án gốc: [rksahu01/VoiceControl-HomeAutomation](https://github.com/rksahu01/VoiceControl-HomeAutomation)*
