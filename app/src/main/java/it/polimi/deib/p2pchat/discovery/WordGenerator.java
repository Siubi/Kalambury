package it.polimi.deib.p2pchat.discovery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;

public class WordGenerator{
    LinkedList<String> words;

    WordGenerator(){
        String[] data = {
                "Prezent",
                "Skrzypce",
                "Samochód",
                "Rycerz",
                "Kwiatek",
                "Telefon",
                "Pociąg",
                "Pianino",
                "Lekarz",
                "Zegar",
                "Dom",
                "Strażak",
                "Policjant"
        };
        List<String> dataList = new ArrayList<String>(Arrays.asList(data));
        Collections.shuffle(dataList);
        words = new LinkedList<>(dataList);
    }

    public String GetWord(){
        return words.removeFirst();
    }

}
