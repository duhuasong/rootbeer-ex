// System includes
#include <stdio.h>
#include <assert.h>

// CUDA runtime
#include <cuda_runtime.h>

#include "UnixStopwatch.h"

__device__ int getThreadId(){
  int blockSize = blockDim.x * blockDim.y * blockDim.z;
    
  int ret = (blockIdx.x * (int) gridDim.y * blockSize) +
            (blockIdx.y * blockSize) +
            (threadIdx.x * blockDim.y * blockDim.z) +
            (threadIdx.y * blockDim.z);
  return ret;
}

__global__ void kernel(float * a, float * b, float * c, int size){

  int threadId = getThreadId();
  int i = threadId / size;
  int j = threadId % size;

  float sum = 0;
  for(int k = 0; k < size; ++k){
    sum += a[i*size+k] * b[k*size+j];
  }
  c[i*size+j] = sum;  
} 

void initAB(float * array, int size){
  int index = 0;
  for(int i = 0; i < size; ++i){
    for(int j = 0; j < size; ++j){
      array[index] = index % 8;
      ++index;
    }
  }
}

float * computeGold(float * a, float * b, int size){
  float * gold_c = (float *) malloc(size*size*sizeof(float));
  
  for(int i = 0; i < size; ++i){
    for(int j = 0; j < size; ++j){
      float sum = 0;
      for(int k = 0; k < size; ++k){
        sum += a[i*size+k] * b[k*size+j];
      }
      gold_c[i*size+j] = sum;
    }
  }
  return gold_c;
}

void checkResults(float * a, float * b, float * c, int size){
  float * gold_c = computeGold(a, b, size);
  int match = 1;
  for(int i = 0; i < size; ++i){
    for(int j = 0; j < size; ++j){
      float lhs = c[i*size+j];
      float rhs = gold_c[i*size+j];
      if(lhs != rhs){
        match = 0;
      }
    }
  }
  if(match){
    printf("  results match\n");
  } else {
    printf("  results mismatch\n");
  }
  free(gold_c);
}

int main(int argc, char * argv[]){
  int size = 2048;
  float * host_a = (float *) malloc(size*size*sizeof(float));
  float * host_b = (float *) malloc(size*size*sizeof(float));
  float * host_c = (float *) malloc(size*size*sizeof(float));

  float * device_a;
  float * device_b;
  float * device_c;

  initAB(host_a, size);
  initAB(host_b, size);  

  cudaError_t error;
  error = cudaMalloc((void **) &device_a, size*size*sizeof(float));
  if(error != cudaSuccess){
    printf("cudaMalloc returned error code %d, line(%d)\n", error, __LINE__);
    exit(0);
  }

  error = cudaMalloc((void **) &device_b, size*size*sizeof(float));
  if(error != cudaSuccess){
    printf("cudaMalloc returned error code %d, line(%d)\n", error, __LINE__);
    exit(0);
  }

  error = cudaMalloc((void **) &device_c, size*size*sizeof(float));
  if(error != cudaSuccess){
    printf("cudaMalloc returned error code %d, line(%d)\n", error, __LINE__);
    exit(0);
  }

  for(int i = 0; i < 8; ++i){
    UnixStopwatch watch;
    watch.start();
  
    error = cudaMemcpy(device_a, host_a, size*size*sizeof(float), cudaMemcpyHostToDevice);
    if(error != cudaSuccess){
      printf("cudaMemcpy returned error code %d, line(%d)\n", error, __LINE__);
      exit(0);
    }

    error = cudaMemcpy(device_b, host_b, size*size*sizeof(float), cudaMemcpyHostToDevice);
    if(error != cudaSuccess){
      printf("cudaMemcpy returned error code %d, line(%d)\n", error, __LINE__);
      exit(0);
    }

    kernel<<<16384, 256>>>(device_a, device_b, device_c, size);
    cudaDeviceSynchronize();

    error = cudaMemcpy(host_c, device_c, size*size*sizeof(float), cudaMemcpyDeviceToHost);
    if(error != cudaSuccess){
      printf("cudaMemcpy returned error code %d, line(%d)\n", error, __LINE__);
      exit(0);
    }
    watch.stop();
    printf("time: %d\n", watch.getTime());

    checkResults(host_a, host_b, host_c, size);
  }
}
