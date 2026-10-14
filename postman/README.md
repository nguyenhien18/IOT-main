# Postman - IoT Room Monitoring

## Import

1. Chay backend tai `http://localhost:8080`.
2. Mo Postman, chon **Import**.
3. Chi can import file `IoT-Room-Monitoring.postman_collection.json`.
4. Khong can chon Environment.
5. Chay request **00 - Authentication / Login - Lay JWT** truoc.

Body Login da co san tai khoan `admin` va mat khau `123456`. Request Login tu dong luu `result.token` vao bien noi bo `jwtToken` cua Collection. Cac request con lai se tu dung token nay.

Tat ca request da ghi truc tiep dia chi backend `http://localhost:8080`. ID cam bien, ID thiet bi va body mau cung da duoc dien san; chi can bam **Send**.

## Thu tu kiem tra de xuat

1. Login - Lay JWT.
2. Lay danh sach va chi tiet cam bien.
3. Lay so do moi nhat.
4. Lich su cam bien.
5. Lay du lieu bieu do.
6. Lay danh sach LED.
7. Bat/Tat LED hoac Bat/Tat tat ca LED (can Mosquitto va ESP8266 dang chay).
8. Danh sach va chi tiet lich su dieu khien.
9. Lay/Cap nhat thong tin ca nhan.

Nhung query parameter dang bi tat trong Postman co the duoc bat bang cach danh dau checkbox ben trai tham so. `page` bat dau tu `0`.

Request **Them mot so do thu cong** va **Cap nhat thong tin ca nhan** se ghi du lieu vao MySQL. Request **Bat/Tat LED** va **Bat/Tat tat ca LED** se dieu khien thiet bi vat ly va tao lich su dieu khien.
