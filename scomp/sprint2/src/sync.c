#include <unistd.h>
#include <stdio.h>
#include "../include/sync.h"
#include <errno.h>


int sync_init(int ctrl_pipes[][2], int n_flights) {
    for (int i = 0; i < n_flights; i++) {
        if (pipe(ctrl_pipes[i]) == -1) {
            perror("pipe ctrl");
            return -1;
        }
    }
    return 0;
}

// Parent writes 1 byte to authorise the child to start the next step
void step_signal(int ctrl_write_fd) {
    char go = 1;
    write(ctrl_write_fd, &go, 1);
}

// Child reads 1 byte, blocks until parent signals
int step_wait(int ctrl_read_fd) {
    char go;
    ssize_t n;
    do {
        n = read(ctrl_read_fd, &go, 1);
    } while (n == -1 && errno == EINTR);
    return (n == 1) ? 0 : -1;
}

// Child closes all control pipe ends except its own read end
void sync_close_child(int ctrl_pipes[][2], int n_flights, int flight_index) {
    for (int i = 0; i < n_flights; i++) {
        if (i == flight_index) {
            close(ctrl_pipes[i][1]); // close write end
        } else {
            close(ctrl_pipes[i][0]); // close read end of other flights
            close(ctrl_pipes[i][1]); // close write end of other flights
        }
    }
}

void sync_cleanup(int ctrl_pipes[][2], int n_flights) {
    for (int i = 0; i < n_flights; i++) {
        close(ctrl_pipes[i][1]);
    }
}