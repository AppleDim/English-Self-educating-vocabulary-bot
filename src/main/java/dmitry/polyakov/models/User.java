package dmitry.polyakov.models;

import dmitry.polyakov.constants.BotStateEnum;
import jakarta.transaction.Transactional;
import lombok.*;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Transactional
@AllArgsConstructor
@Table(name = "users", schema = "telegram")
public class User {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "registered_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp registeredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_bot_state")
    private BotStateEnum userBotState;

    @ManyToMany(mappedBy = "users", cascade = CascadeType.DETACH)
    private Set<Phrase> phrases = new HashSet<>();
}
