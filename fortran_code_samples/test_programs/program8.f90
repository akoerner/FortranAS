! Program 8: File I/O Example
program program8
  integer :: i, numbers(5)

  open(unit=10, file='data.txt', status='old')

  do i = 1, 5
    read(10, *) numbers(i)
  end do

  close(10)

  print *, 'Read numbers:', numbers
end program program8


