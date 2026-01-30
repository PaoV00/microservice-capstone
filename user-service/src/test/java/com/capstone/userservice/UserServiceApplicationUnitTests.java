package com.capstone.userservice;

import com.capstone.userservice.model.Address;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;

public class UserServiceApplicationUnitTests {




    // create user and return its USERID
//    private Long createUser(String firstName, String lastName,
//                              String username, String password,
//                              String email, Address address) {
//        Map<String, Object> payload = Map.of(
//                "firstName" , firstName
//                , "lastName" , lastName
//                , "username" , username
//                , "password" , password
//                , "email" , email
//                , "address" , address
//        );
//
//        return given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body(payload)
//                .when()
//                .post("/api/user")
//                .then()
//                .statusCode(HttpStatus.CREATED.value())
//                .contentType(ContentType.JSON)
//                .body("firstName", equalTo(firstName))
//                .body("userId", notNullValue())
//                .extract().path("userId");
//    }
//
//    private Address createAddress(String number, String street, String city, String stateCode, String zip, String countryCode) {
//        return Address.builder()
//                .number(number)
//                .street(street)
//                .city(city)
//                .stateCode(stateCode)
//                .zip(zip)
//                .countryCode(countryCode)
//                .build();
//    }
//
//    private void deleteById(Long userId) {
//        given()
//                .when()
//                .delete("/api/user/" + userId)
//                .then()
//                .statusCode(HttpStatus.NO_CONTENT.value());
//    }
//
//    @Test
//    void addUser_return201_AndGetUser_return200(){
//        Address address = createAddress("123", "street", "city", "stateCode", "zip", "countryCode");
//        Long userId = createUser("firstName", "lastName",
//                "username", "password", "first.last@test.com", address);
//
//        given()
//                .when()
//                .get("/api/user/" + userId)
//                .then()
//                .statusCode(HttpStatus.OK.value());
//
//        deleteById(userId);
//    }
}
