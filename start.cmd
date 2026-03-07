@echo off
REM RentEase: Migrate then start backend (bypasses PowerShell execution policy)
powershell -ExecutionPolicy Bypass -NoProfile -File "%~dp0start.ps1"
