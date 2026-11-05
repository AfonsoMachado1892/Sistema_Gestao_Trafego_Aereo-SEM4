CC      = gcc
CFLAGS  = -Wall -Wextra -g -pthread
LDFLAGS = -pthread -lm

SRC_DIR = src
INC_DIR = include

SRCS = $(SRC_DIR)/main.c          \
       $(SRC_DIR)/shm.c           \
       $(SRC_DIR)/semaphore_sync.c \
       $(SRC_DIR)/threads.c        \
       $(SRC_DIR)/flight_process.c \
       $(SRC_DIR)/physics.c        \
       $(SRC_DIR)/json_parser.c    \
       $(SRC_DIR)/report.c         \
       $(SRC_DIR)/safety.c

OBJS = $(SRCS:.c=.o)

TARGET = simulation

.PHONY: all clean

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

$(SRC_DIR)/%.o: $(SRC_DIR)/%.c
	$(CC) $(CFLAGS) -I$(INC_DIR) -c $< -o $@

clean:
	rm -f $(SRC_DIR)/*.o $(TARGET) simulation_report.txt
	rm -f /dev/shm/aisafe_sim /dev/shm/sem.aisafe_*
