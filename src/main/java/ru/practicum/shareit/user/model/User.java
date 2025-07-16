package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Имя владельца не может быть пустым")
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 512)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @")
    private String email;
}
