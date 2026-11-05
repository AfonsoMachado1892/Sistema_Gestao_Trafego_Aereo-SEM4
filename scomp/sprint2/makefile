CC      = gcc
CFLAGS  = -Wall -Wextra -Wpedantic -std=c11
LDFLAGS = -lm -lrt

SRC_DIR = src
INC_DIR = include

SRCS = $(SRC_DIR)/main.c         \
       $(SRC_DIR)/json_parser.c  \
       $(SRC_DIR)/physics.c      \
       $(SRC_DIR)/flight_process.c \
       $(SRC_DIR)/sync.c         \
       $(SRC_DIR)/safety.c       \
       $(SRC_DIR)/report.c

TARGET = simulation

$(TARGET): $(SRCS)
	$(CC) $(CFLAGS) -I$(INC_DIR) $(SRCS) -o $(TARGET) $(LDFLAGS)

clean:
	rm -f $(TARGET)

.PHONY: clean