typedef struct This_s{
   __global float *val$matrixA;
   __global float *val$matrixB;
   __global float *val$matrixC;
   int passid;
}This;
int get_pass_id(This *this){
   return this->passid;
}
__kernel void run(
   __global float *val$matrixA, 
   __global float *val$matrixB, 
   __constant float *shared, 
   __global float *val$matrixC, 
   int passid
){
   This thisStruct;
   This* this=&thisStruct;
   this->val$matrixA = val$matrixA;
   this->val$matrixB = val$matrixB;
   this->val$matrixC = val$matrixC;
   this->passid = passid;
   __local float shared1[512];
   {
      int threadIdx = get_global_id(0);
      int threadIdy = get_global_id(1);
      int blockIdxx = threadIdx / 16;
      int blockIdxy = threadIdy / 16;
      int threadIdxx = threadIdx % 16;
      int threadIdxy = threadIdy % 16;
      int width = 2048;
      int aBegin = (width * 16) * blockIdxy;
      int aEnd = (aBegin + width) - 1;
      int aStep = 16;
      int bBegin = 16 * blockIdxx;
      int bStep = 16 * width;
      float sum = 0.0f;
      int arrayIndex = (width * threadIdxy) + threadIdxx;
      int a = aBegin;
      int b = bBegin;
      for (; a<=aEnd; b = b + bStep){
         float valueA = this->val$matrixA[(a + arrayIndex)];
         float valueB = this->val$matrixB[(b + arrayIndex)];
         int indexA = (threadIdxy * 16) + threadIdxx;
         int indexB = 256 + ((threadIdxy * 16) + threadIdxx);
         shared1[indexA]  = valueA;
         shared1[indexB]  = valueB;
         barrier(CLK_LOCAL_MEM_FENCE);
         for (int k = 0; k<16; k++){
            indexA = (threadIdxy * 16) + k;
            indexB = 256 + ((k * 16) + threadIdxx);
            valueA = shared1[indexA];
            valueB = shared1[indexB];
            sum = sum + (valueA * valueB);
         }
         barrier(CLK_LOCAL_MEM_FENCE);
         a = a + aStep;
      }
      int c = ((width * 16) * blockIdxy) + (16 * blockIdxx);
      this->val$matrixC[c + arrayIndex]  = sum;
      return;
   }
}
