program MultiBranchIf
    implicit none
    integer :: score

    write(*, *) "Enter your test score:"
    read(*, *) score

    if (score >= 90) then
        write(*, *) "Grade: A"
    else if (score >= 80) then
        write(*, *) "Grade: B"
    else if (score >= 70) then
        write(*, *) "Grade: C"
    else if (score >= 60) then
        write(*, *) "Grade: D"
    else
        write(*, *) "Grade: F"
    end if
end program MultiBranchIf
