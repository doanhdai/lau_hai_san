@echo off
REM Script batch để chạy update constraint cho dishes status
REM Thay đổi các thông tin kết nối phù hợp với môi trường của bạn

echo ========================================
echo Cap nhat CHECK constraint cho dishes.status
echo ========================================
echo.

REM Thay đổi các thông tin sau cho phù hợp:
REM -S: Tên server SQL Server (localhost hoặc tên server)
REM -d: Tên database
REM -E: Sử dụng Windows Authentication (hoặc dùng -U username -P password)
REM -i: Đường dẫn đến file SQL script

sqlcmd -S localhost -d QuanLyNhaHangLau -E -i "update_dishes_status_constraint.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Cap nhat thanh cong!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Co loi xay ra! Vui long kiem tra lai.
    echo ========================================
)

pause

