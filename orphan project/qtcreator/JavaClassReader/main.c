#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

uint32_t readUint32(void *data) {
  uint32_t value;
  uint8_t *big = data;
  uint8_t *little = (uint8_t *)&value;

  for (int i = 0; i < 4; ++i) {
    little[i] = big[3 - i];
  }

  return value;
}

uint16_t readUint16(void *ptr) {
  uint16_t value;
  uint8_t *big = ptr;
  uint8_t *little = (uint8_t *)&value;

  little[0] = big[1];
  little[1] = big[0];

  return value;
}

void checkJava(uint8_t *data) {
  uint32_t magic = readUint32(data);
  if (magic == 0xcafebabe) {
    printf("We have a java class file\n");
  } else {
    printf("I don't touch that: %04x\n", magic);
    exit(3);
  }
}

uint32_t getMagic(void *data) { return readUint32(data); }

size_t minorVersionOffset() { return 4; }

uint16_t getMinorVersion(void *data) {
  return readUint16(data + minorVersionOffset());
}

size_t majorVersionOffset() { return minorVersionOffset() + 2; }

uint16_t getMajorVersion(void *data) {
  return readUint16(data + majorVersionOffset());
}

size_t constantPoolCountOffset() { return majorVersionOffset() + 2; }

uint16_t getContantPoolCount(void *data) {
  uint16_t size = readUint16(data + constantPoolCountOffset());
  if (size == 0) {
    printf("Invalid const pool size: 0");
    exit(0);
  }
  return size;
}

char *readAsCONSTANT_Utf8_info(void *data, size_t offset) {
  uint8_t tag = *(uint8_t *)(data + offset);
  if (tag != 1) {
    printf("Invalid offset");
    exit(1);
  }
  uint16_t length = readUint16(data + offset + 1);
  char *value = malloc(length + 1);
  value[length] = 0;
  memcpy(value, data + offset + 1 + 2, length);
  return value;
}

size_t loadAsCONSTANT_Methodref(void *data, size_t offset) {
  uint8_t tag = *(uint8_t *)(data + offset);
  if (tag != 10) {
    printf("Invalid offset. Method ref tag=%d", tag);
    exit(1);
  }
  uint16_t class_index = readUint16(data + offset + 1);
  uint16_t name_and_type_index = readUint16(data + offset + 1 + 2);

  printf("Read class at %04x", class_index);
  char *value = readAsCONSTANT_Utf8_info(data, class_index);
  printf("Method class: %s", value);
  free(value);

//  char *value = readAsCONSTANT_NameAndType_info(data, class_index);
//  printf("Method class: %s", value);
//  free(value);

  return offset + 5;
}

void printConstantPoolContent(void *data) {
  size_t start = constantPoolCountOffset() + 2;
  size_t currentOffset = start;
  uint8_t tag = *(uint8_t*)(data + currentOffset);
  switch (tag) {
  case 10:
    currentOffset = loadAsCONSTANT_Methodref(data, currentOffset);
    break;
  default:
    printf("The tag with id %d is unknown.", tag);
  }
}

void print(void *data) {
  checkJava(data);

  printf("I'm a %08x\n", getMagic(data));
  printf("Minor version: %04x\n", getMinorVersion(data));
  printf("Major version: %04x\n", getMajorVersion(data));
  printf("Constant pool entries: %d\n", getContantPoolCount(data));
  printConstantPoolContent(data);
}

int main(int argc, const char *argv[]) {
  //  if(argc < 2)
  //  {
  //    printf("First argument must be the path to the .class file");
  //    exit(1);
  //  }
  //  const char* location = argv[1];

  const char *location = "./ex.class";

  FILE *file = fopen(location, "rb");

  if (file == NULL) {
    printf("Can't open file %s", location);
  }

  fseek(file, 0L, SEEK_END);
  size_t size = ftell(file);
  rewind(file);

  uint8_t *data = (uint8_t *)malloc(size);

  if (data == NULL) {
    printf("Can't allocate %lu bytes", size);
    exit(2);
  }

  uint32_t c;
  size_t index = 0;
  while ((c = fgetc(file)) != EOF) {
    data[index] = (uint8_t)c;
    index++;
  }

  fclose(file);

  print(data);

  return 0;
}
