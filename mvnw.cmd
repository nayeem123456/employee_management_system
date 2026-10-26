@echo off
@REM ==============================================================================
@REM Maven Wrapper Batch Script for AI-Powered Employee Management System (NexusHR)
@REM ==============================================================================
setlocal

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    call mvn %*
    goto :end
)

if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        call "%MAVEN_HOME%\bin\mvn.cmd" %*
        goto :end
    )
)

if defined M2_HOME (
    if exist "%M2_HOME%\bin\mvn.cmd" (
        call "%M2_HOME%\bin\mvn.cmd" %*
        goto :end
    )
)

if exist "%USERPROFILE%\tools\apache-maven-3.9.6\bin\mvn.cmd" (
    call "%USERPROFILE%\tools\apache-maven-3.9.6\bin\mvn.cmd" %*
    goto :end
)

echo [ERROR] Maven not found. Please install Apache Maven and ensure 'mvn' is on your PATH, or set MAVEN_HOME.
exit /b 1

:end
exit /b %ERRORLEVEL%
