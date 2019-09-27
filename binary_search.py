if __name__ == '__main__':
    items = [int(x) for x in input('Items: ').strip().split()]
    key = int(input('Key: ').strip())

    
    left = 0
    right = len(items)
    while left < right:
        mid = (left + right) // 2
        if items[mid] >= key:
            right = mid
        else:
            left = mid + 1
    # left -= 1
    print(items)
    print('found: ', left, right)
    print(items[left])