#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>
#include "../include/shm.h"

SimState *shm_create(void) {
    /* Remove objeto anterior caso tenha ficado órfão */
    shm_unlink(SHM_NAME);

    int fd = shm_open(SHM_NAME, O_CREAT | O_EXCL | O_RDWR, S_IRUSR | S_IWUSR);  // cria o objeto em si mas não cria a memoria partilhada (mmap)
    if (fd == -1) {
        perror("shm_open (create)");
        return NULL;
    }

    if (ftruncate(fd, sizeof(SimState)) == -1) {    //reserva a memória física para a memória partilhada
        perror("ftruncate");
        close(fd);
        shm_unlink(SHM_NAME);
        return NULL;
    }

    SimState *state = (SimState *)mmap(NULL, sizeof(SimState),
                                       PROT_READ | PROT_WRITE,
                                       MAP_SHARED, fd, 0);  // cria a ligação entre o objeto na dev/shm e o processo
    close(fd);  // fd já não é preciso, agora usa-se o pointeiro *state

    if (state == MAP_FAILED) {
        perror("mmap (create)");
        shm_unlink(SHM_NAME);
        return NULL;
    }

    memset(state, 0, sizeof(SimState)); // limpa para garantir que não tem bytes lixo
    return state;
}

void shm_detach(SimState *state) {
    if (state != NULL)
        munmap(state, sizeof(SimState));
}

void shm_destroy(void) {
    shm_unlink(SHM_NAME);
}