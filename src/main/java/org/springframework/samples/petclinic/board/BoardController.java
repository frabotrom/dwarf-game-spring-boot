package org.springframework.samples.petclinic.board;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService dwarfService) {
        this.boardService = dwarfService;
    }

    @GetMapping(value = "/board")
    public String welcome(Map<String, Object> model, HttpServletResponse response) {
        // response.addHeader("Refresh","3");
        model.put("now", new Date());
        // model.put("board", boardService.findById(1).get());
        model.put("board", BoardService.createBoard());
        return "game/board";
    }

}