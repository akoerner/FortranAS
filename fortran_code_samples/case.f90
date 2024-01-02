program Case
    implicit none
    integer :: choice

    write(*, *) "Enter a choice (1, 2, or 3):"
    read(*, *) choice

    select case (choice)
    case (1)
        write(*, *) "You selected option 1"
    case (2)
        write(*, *) "You selected option 2"
    case (3)
        write(*, *) "You selected option 3"
    case default
        write(*, *) "Invalid choice"
    end select
end program Case
