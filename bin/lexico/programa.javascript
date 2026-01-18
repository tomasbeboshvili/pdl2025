// Programa de prueba adaptado a la nueva gramática
let int a;
let float b;

a = 1;
b = 10.5;

// Bucle for
for (a = 1; a == 5; a = a + 1) {
    b /= 2;
}

let string s;
s = 'Hola Mundo';

// Función con tipo de retorno (void no existe en la nueva gramática, usamos int por ejemplo)
function int saludar() {
    return 0;
}

saludar();
