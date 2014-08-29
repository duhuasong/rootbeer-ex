package org.trifort.rootbeer.sort;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;


public class GPUSortKernel implements Kernel {

  private int[] array;

  public GPUSortKernel(int[] array){
    this.array = array;
  }

  @Override
  public void gpuMethod(){
    int index1a = RootbeerGpu.getThreadIdxx() * 2;
    int index1b = (RootbeerGpu.getThreadIdxx() * 2) + 1;
    int index2a = index1a - 1;
    int index2b = index1b - 1;

    RootbeerGpu.setSharedInteger(4*index1a,array[index1a]);
    RootbeerGpu.setSharedInteger(4*index1b,array[index1b]);
    //outer pass
    for(int i = 0; i < array.length; ++i){
      int value1 = RootbeerGpu.getSharedInteger(4*index1a);
      int value2 = RootbeerGpu.getSharedInteger(4*index1b);
      if(value2 < value1){
        RootbeerGpu.setSharedInteger(4*index1a, value2);
        RootbeerGpu.setSharedInteger(4*index1b, value1);
      }
      RootbeerGpu.syncthreads();
      if(index2a > 0){
        value1 = RootbeerGpu.getSharedInteger(4*index2a);
        value2 = RootbeerGpu.getSharedInteger(4*index2b);
        if(value2 < value1){
          RootbeerGpu.setSharedInteger(4*index2a, value2);
          RootbeerGpu.setSharedInteger(4*index2b, value1);
        }
      }
      RootbeerGpu.syncthreads();
    }
    array[index1a] = RootbeerGpu.getSharedInteger(4*index1a);
    array[index1b] = RootbeerGpu.getSharedInteger(4*index1b);
  }
}

/*
template<std::size_t bound,
         std::size_t grainsize,
         typename RandomAccessIterator1,
         typename RandomAccessIterator2,
         typename Compare>
__forceinline__ __device__
void stable_odd_even_transpose_sort_by_key(const bounded<bound,agent<grainsize> > &,
                                           RandomAccessIterator1 keys_first, RandomAccessIterator1 keys_last,
                                           RandomAccessIterator2 values_first,
                                           Compare comp)
{
  stable_odd_even_transpose_sort_by_key_impl<0, bound>::sort(keys_first, values_first, keys_last - keys_first, comp);
} // end stable_odd_even_transpose_sort_by_key()

template<int i, int bound>
struct stable_odd_even_transpose_sort_by_key_impl
{
  template<typename RandomAccessIterator1, typename RandomAccessIterator2, typename Compare>
  static __device__
  void sort(RandomAccessIterator1 keys, RandomAccessIterator2 values, int n, Compare comp)
  {
    for(int j = 1 & i; j < bound - 1; j += 2)
    {
      if(j + 1 < n && comp(keys[j + 1], keys[j]))
      {
        using thrust::swap;

      	swap(keys[j], keys[j + 1]);
      	swap(values[j], values[j + 1]);
      }
    }

    stable_odd_even_transpose_sort_by_key_impl<i + 1, bound>::sort(keys, values, n, comp);
  }
};
*/
