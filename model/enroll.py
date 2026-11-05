"""
Chụp ảnh đăng ký người nhà bằng webcam.
    python enroll.py --name Ba
SPACE = chụp 1 ảnh, Q/ESC = xong. Ảnh lưu vào family/<tên>/
Nên chụp 3-5 ảnh: nhìn thẳng, hơi nghiêng trái/phải, ánh sáng khác nhau.
"""
import argparse
import os
import time

import cv2
import numpy as np

ap = argparse.ArgumentParser()
ap.add_argument("--name", required=True, help="tên người nhà (tên thư mục)")
ap.add_argument("--cam", type=int, default=0)
args = ap.parse_args()

folder = os.path.join("family", args.name)
os.makedirs(folder, exist_ok=True)
cap = cv2.VideoCapture(args.cam)
if not cap.isOpened():
    raise SystemExit("Không mở được webcam")

n = len(os.listdir(folder))
while True:
    ok, frame = cap.read()
    if not ok:
        break
    vis = frame.copy()
    cv2.putText(vis, f"{args.name}: da chup {n} anh | SPACE=chup  Q=xong", (10, 28),
                cv2.FONT_HERSHEY_SIMPLEX, 0.65, (0, 215, 255), 2, cv2.LINE_AA)
    cv2.imshow("Dang ky nguoi nha", vis)
    k = cv2.waitKey(1) & 0xFF
    if k == ord(" "):
        path = os.path.join(folder, f"{int(time.time() * 1000)}.jpg")
        ok2, buf = cv2.imencode(".jpg", frame)
        if ok2:
            buf.tofile(path)
            n += 1
            print("Đã lưu", path)
    elif k in (ord("q"), 27):
        break
cap.release()
cv2.destroyAllWindows()
print(f"Xong. {args.name} có {n} ảnh trong {folder}/")
