package org.springframework.samples.petclinic.game;

import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.playerState.PlayerState;

/*
* Clase tipo wrapper que contiene los datos actuales del tablero
*/

@Getter
@Setter
public class BoardData{
    public String currentUser;

    //Equates to where does the player put the worker
    public Integer playerAction;
}
