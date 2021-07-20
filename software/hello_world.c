
#include <stdio.h>

int main() {
  int a = 1;
  int b = 2;
  int c = a + b;

  int x = 6;
  int y = 7;
  int z = x * y;

  char *out_buffer = (char *)0xa0000000;
  sprintf(out_buffer, "%d + %d = %d, %d * %d = %d\n", a, b, c, x, y, z);
}
