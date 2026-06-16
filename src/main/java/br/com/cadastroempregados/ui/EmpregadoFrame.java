package br.com.cadastroempregados.ui;

import br.com.cadastroempregados.aplicacao.EmpregadoService;
import br.com.cadastroempregados.modelo.Empregado;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.RoundingMode;
import java.util.List;

public class EmpregadoFrame extends JFrame {
    private final EmpregadoService service;
    private final JTextField nomeField = new JTextField(20);
    private final JTextField cpfField = new JTextField(14);
    private final JTextField salarioField = new JTextField(10);
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Nome", "CPF", "Salario"}, 0);

    public EmpregadoFrame(EmpregadoService service) {
        this.service = service;

        setTitle("Cadastro de Empregados");
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(criarFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        atualizarTabela();
    }

    private JPanel criarFormulario() {
        JPanel camposPanel = new JPanel(new GridLayout(3, 2, 4, 4));
        camposPanel.add(new JLabel("Nome:"));
        camposPanel.add(nomeField);
        camposPanel.add(new JLabel("CPF:"));
        camposPanel.add(cpfField);
        camposPanel.add(new JLabel("Salario:"));
        camposPanel.add(salarioField);

        JButton salvarButton = new JButton("Salvar");
        salvarButton.addActionListener(event -> salvarEmpregado());

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoesPanel.add(salvarButton);

        JPanel formularioPanel = new JPanel(new BorderLayout(8, 8));
        formularioPanel.add(camposPanel, BorderLayout.CENTER);
        formularioPanel.add(botoesPanel, BorderLayout.SOUTH);

        return formularioPanel;
    }

    private void salvarEmpregado() {
        try {
            service.inserir(nomeField.getText(), cpfField.getText(), salarioField.getText());
            limparCampos();
            atualizarTabela();
            JOptionPane.showMessageDialog(this, "Empregado cadastrado com sucesso.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dados invalidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limparCampos() {
        nomeField.setText("");
        cpfField.setText("");
        salarioField.setText("");
        nomeField.requestFocus();
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);

        List<Empregado> empregados = service.listar();
        for (Empregado empregado : empregados) {
            tableModel.addRow(new Object[]{
                    empregado.getNome(),
                    empregado.getCpf(),
                    empregado.getSalario().setScale(2, RoundingMode.HALF_UP)
            });
        }
    }
}
