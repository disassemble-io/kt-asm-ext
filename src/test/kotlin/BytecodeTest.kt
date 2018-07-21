fun x(num: Int) : Int {
    return num * 2
}

fun y(num: Int): Int {
    if (num % 2 == 0) {
        return x(num) * x(num) + x(num)
    } else {
        return x(num) * x(num) - x(num)
    }
}