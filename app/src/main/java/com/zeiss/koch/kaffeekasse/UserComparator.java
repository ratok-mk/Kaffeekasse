package com.zeiss.koch.kaffeekasse;

import java.util.Comparator;

/**
 * Created by ogmkoch on 07.11.2016.
 */

public class UserComparator implements Comparator<User> {
    @Override
    public int compare(User o1, User o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
