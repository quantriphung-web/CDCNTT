"""
Script kiểm tra nhận diện khuôn mặt trực tiếp qua Webcam Laptop
Sử dụng các ảnh người nhà đã đăng ký từ app điện thoại (trong thư mục family/).
"""
import sys
import os
import cv2
import time
from family_faces import FaceID, ascii_name

def main():
    print("=" * 60)
    print("  KIỂM TRA NHẬN DIỆN KHUÔN MẶT QUA WEBCAM LAPTOP")
    print("=" * 60)
    
    # Khởi tạo bộ nhận diện khuôn mặt
    faceid = FaceID(family_dir="family", threshold=0.363)
    
    if not faceid.db:
        print("[!] Cảnh báo: Chưa có người nhà nào trong thư mục 'family/'!")
        return

    # Mở webcam laptop (source 0)
    print("[*] Đang khởi động webcam laptop...")
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("[X] Không mở được webcam laptop (camera 0)!")
        return

    # Tối ưu kích thước khung hình
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

    print("[OK] Webcam đã mở! Nhấn phím 'q' hoặc 'ESC' để thoát.")
    
    prev_time = time.time()
    
    while True:
        ret, frame = cap.read()
        if not ret:
            print("[X] Mất tín hiệu webcam!")
            break

        # Lật ảnh gương (mirror) cho tự nhiên khi soi webcam
        frame = cv2.flip(frame, 1)
        h, w = frame.shape[:2]

        # Phát hiện khuôn mặt
        faces = faceid._detect(frame)

        # Tính FPS
        curr_time = time.time()
        fps = 1.0 / max(curr_time - prev_time, 1e-5)
        prev_time = curr_time

        # Duyệt qua các khuôn mặt phát hiện được
        for face_row in faces:
            score = face_row[-1]
            if score < faceid.det_score:
                continue

            # Tọa độ khuôn mặt
            bx = int(face_row[0])
            by = int(face_row[1])
            bw = int(face_row[2])
            bh = int(face_row[3])
            
            # Trích xuất đặc trưng
            feat = faceid._embed(frame, face_row)

            # So khớp với database
            best_name = None
            best_score = -1.0

            for name, feats in faceid.db.items():
                for f_ref in feats:
                    sim = faceid.rec.match(feat, f_ref, cv2.FaceRecognizerSF_FR_COSINE)
                    if sim > best_score:
                        best_score = sim
                        best_name = name

            # Kiểm tra ngưỡng (threshold mặc định 0.363)
            if best_score >= faceid.threshold and best_name is not None:
                color = (0, 255, 0) # Xanh lá: Người nhà
                percent = min(100.0, (best_score / 0.6) * 100.0)
                label = f"NGUOI NHA: {ascii_name(best_name)} ({best_score:.2f})"
            else:
                color = (0, 0, 255) # Đỏ: Người lạ
                label = f"NGUOI LA ({best_score:.2f})"

            # Vẽ khung và nhãn
            cv2.rectangle(frame, (bx, by), (bx + bw, by + bh), color, 2)
            cv2.rectangle(frame, (bx, by - 28), (bx + bw, by), color, -1)
            cv2.putText(frame, label, (bx + 6, by - 8),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2, cv2.LINE_AA)

        # Thanh tiêu đề trên cùng
        cv2.rectangle(frame, (0, 0), (w, 40), (20, 20, 20), -1)
        cv2.putText(frame, f"HE THONG NHAN DIEN NGUOI NHA | FPS: {fps:.1f} | Bam 'Q' de thoat",
                    (15, 26), cv2.FONT_HERSHEY_SIMPLEX, 0.65, (0, 255, 255), 2, cv2.LINE_AA)

        # Hiển thị cửa sổ
        cv2.imshow("Xac thuc khuon mat - Laptop Webcam", frame)

        key = cv2.waitKey(1) & 0xFF
        if key == ord('q') or key == ord('Q') or key == 27:
            break

    cap.release()
    cv2.destroyAllWindows()
    print("[*] Đã tắt webcam.")

if __name__ == "__main__":
    main()

