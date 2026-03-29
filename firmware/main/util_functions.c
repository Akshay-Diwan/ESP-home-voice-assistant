#include <stdint.h>

int16_t find_median(int16_t *array, int n) {
    // 1. Create a temporary copy so we don't ruin the original "Order of Time"
    int16_t temp[100]; 
    for(int i = 0; i < n; i++) temp[i] = array[i];

    // 2. Selection Sort (Efficient for small N)
    for (int i = 0; i < n - 1; i++) {
        int min_idx = i;
        for (int j = i + 1; j < n; j++) {
            if (temp[j] < temp[min_idx]) {
                min_idx = j;
            }
        }
        // Swap the found minimum element with the first element
        int16_t swap_var = temp[min_idx];
        temp[min_idx] = temp[i];
        temp[i] = swap_var;
    }

    // 3. Return the middle element
    // For 60 elements, index 30 is the middle
    return temp[n / 2];
}