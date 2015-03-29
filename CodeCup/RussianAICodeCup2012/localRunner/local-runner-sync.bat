:loop

echo "start"

start /WAIT javaw -cp ".;*;%~dp0/*" -jar "local-runner.jar" true true 1 result.txt true true true

echo "end"

timeout /T 2

goto loop