! Program 9: Matrix Multiplication
program program9
  integer, parameter :: n = 3
  integer :: A(n, n), B(n, n), C(n, n)
  integer :: i, j, k

  ! Initialize matrices A and B

  ! Matrix multiplication
  do i = 1, n
    do j = 1, n
      C(i, j) = 0
      do k = 1, n
        C(i, j) = C(i, j) + A(i, k) * B(k, j)
      end do
    end do
  end do

  ! Print result matrix C
  print *, 'Result matrix C:'
  do i = 1, n
    print *, C(i, :)
  end do
end program program9


