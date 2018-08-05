package com.github.bademux.ghfecher.controller;

import com.github.bademux.ghfecher.controller.BasicAuthorizationFilter.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class BasicAuthorizationFilterTest {

    @Parameters(name = "{index}: for header {1} expecting result is {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {User.of("user", "notexists"), createHeader("user", "exists"), false},
                {User.of("user", "exists"), createHeader("user", "exists"), true},
                {User.of("user", ""), createHeader("user", ""), true},
                {User.of("", "exists"), createHeader("", "exists"), true},
                {User.of("user", "exists"), "bad header", false},
                {User.of("user", "exists"), "basic badheader", false},
                {User.of("user", "exists"), "basic " + createHeader("no:"), false}

        });
    }

    @Parameterized.Parameter
    public User user;
    @Parameterized.Parameter(1)
    public String headerValue;

    @Parameterized.Parameter(2)
    public boolean expectedResult;

    @Test
    public void testIsAuthorized() {
        //prepare
        BasicAuthorizationFilter filter = new BasicAuthorizationFilter(Collections.singleton(user));
        //test
        boolean authorized = filter.isAuthorized(headerValue);
        //verify
        assertEquals(expectedResult, authorized);
    }

    private static String createHeader(String login, String password) {
        String tokenStr = login + ":" + password;
        return createHeader(tokenStr);
    }

    private static String createHeader(String tokenStr) {
        byte[] token = tokenStr.getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64Utils.encodeToString(token);
    }
}