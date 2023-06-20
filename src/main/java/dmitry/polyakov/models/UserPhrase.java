package dmitry.polyakov.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Table(name = "users_phrases", schema = "telegram",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "phrase_id"})})
public class UserPhrase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_phrases_id")
    private Long userPhraseId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "phrase_id")
    private Phrase phrase;

    @Column(name = "count_phrase_views")
    private int countPhraseViews;
}
