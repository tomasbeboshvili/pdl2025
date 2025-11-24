# Casos de prueba (fuente, errores, parse)

## Caso 1 (OK)
**Fuente**
```
let int a;
a = 1;
```
**Errores**
```
Sin errores sintácticos detectados.
```
**Parse numérico**
```
descendente 1 4 12 1 7 42 21 24 33 29 26 23 3
```

## Caso 2 (OK)
**Fuente**
```
let int i;
function int inc(int x) { 
    return x + 1; 
}
for (i = 0; i == 10; i = i + 1) { 
    write i;
}
```
**Errores**
```
Sin errores sintácticos detectados.
```
**Parse numérico**
```
descendente 1 4 12 2 8 11 12 17 13 20 57 47 51 3 6 52 48 21 24 33 29 26 23 22 24 27 30 29 25 24 33 29 26 23 23 54 52 48 21 24 30 28 30 29 26 23 23 57 44 21 24 30 29 26 23 58 10 3 3
```

## Caso 3 (ERROR)
**Fuente**
```
let int a;
for (a = 1; a = 2; a = a + 1) {
    write a;
}
```
**Errores**
```
[ERROR - Línea 2]: Falta ';' tras condición de for
[ERROR - Línea 2]: Falta ')' tras incremento de for
[ERROR - Línea 2]: Se esperaba '{' tras cabecera de for
[ERROR - Línea 2]: Falta ';'
[ERROR - Línea 2]: Se esperaba '}' al cerrar el for
```
**Parse numérico**
```
descendente 1 4 12 6 52 48 21 24 33 29 26 23 22 24 27 30 29 26 23 54 52 48 21 24 30 28 30 29 26 23 23 3
```

## Caso 4 (OK)
**Fuente**
```
let boolean ok;
if (true) write ok;
```
**Errores**
```
Sin errores sintácticos detectados.
```
**Parse numérico**
```
descendente 1 4 14 1 5 21 24 27 36 29 26 23 44 21 24 27 30 29 26 23 3
```

## Caso 5 (ERROR)
**Fuente**
```
let int a
a = 1;
```
**Errores**
```
[ERROR - Línea 2]: Falta ';' tras declaración
```
**Parse numérico**
```
descendente 1 4 12 3
```

## Caso 6 (ERROR)
**Fuente**
```
function int bad(int a { return a; }
```
**Errores**
```
[ERROR - Línea 1]: Falta ')' en la cabecera
[ERROR - Línea 1]: Falta '{' antes del cuerpo
[ERROR - Línea 1]: Se esperaba '=' o '/='
[ERROR - Línea 1]: Expresión no válida
```
**Parse numérico**
```
descendente 2 8 11 12 17 12 20 57 7 42 21 24 27 29 26 23 58 10 3
```
