package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.service.AtendimentoService;
import br.com.dimdim.atendimento.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final ClienteService clientes;
    private final AtendimentoService atendimentos;

    public HomeController(ClienteService clientes, AtendimentoService atendimentos) {
        this.clientes = clientes;
        this.atendimentos = atendimentos;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("totalClientes", clientes.contar());
        model.addAttribute("totalAtendimentos", atendimentos.contar());
        return "index";
    }
}
