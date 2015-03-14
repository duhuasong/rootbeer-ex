#ifndef UNIX_STOPWATCH_H
#define UNIX_STOPWATCH_H

#if (defined linux || defined __APPLE_CC__)

#include <sys/time.h>

class UnixStopwatch {
public:
  UnixStopwatch();
  void start();
  void stop();
  long getTime();
private:
  timeval m_StartTime;
  long m_Time;
};

#endif
#endif

