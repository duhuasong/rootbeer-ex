#include "UnixStopwatch.h"

#ifdef linux 
UnixStopwatch::UnixStopwatch(){
  gettimeofday(&m_StartTime, 0);
}

void UnixStopwatch::start(){
  gettimeofday(&m_StartTime, 0);
}

void UnixStopwatch::stop(){
  timeval endTime;
  long seconds, useconds;
  
  gettimeofday(&endTime, 0);

  seconds  = endTime.tv_sec  - m_StartTime.tv_sec;
  useconds = endTime.tv_usec - m_StartTime.tv_usec;

  m_Time = (seconds * 1000) + (useconds / 1000);
}

long UnixStopwatch::getTime(){
  return m_Time;
}

#endif