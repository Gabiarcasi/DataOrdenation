package com.mycompany.speed_sort;


import javax.swing.*; //É usado para criar interfaces gráficas em Java.
import javax.swing.JOptionPane; //Caixas de diálogo.
import java.io.IOException; //Lida com exceções de entrada/saída.
import javax.imageio.ImageIO; //Lê e grava imagens.
import java.awt.Graphics2D; //Realiza operações gráficas avançadas.
import java.io.ByteArrayOutputStream; //Grava dados em um array de bytes em memória
import java.awt.image.BufferedImage; //Representa imagens em formato de bitmap.
import java.sql.ResultSet; //Manipula resultados de consultas em bancos de dados.
import javax.swing.filechooser.FileNameExtensionFilter; //Cria filtros para seleção de tipos de arquivo.
import java.awt.Image; //Representa imagens em interfaces gráficas.
import java.io.File; //Lida com arquivos e diretórios.
import java.sql.Connection; //Estabelece conexão com bancos de dados.
import java.sql.DriverManager; //Gerencia conexões de banco de dados.
import java.sql.PreparedStatement; //Executa instruções SQL predefinidas.
import java.sql.SQLException; // Lida com exceções relacionadas a bancos de dados.
import javax.swing.table.DefaultTableModel; //Representa o modelo de dados de uma tabela.
import javax.swing.table.TableRowSorter; //Classifica e filtra dados de uma tabela.
import java.awt.event.MouseAdapter; //Lida com eventos relacionados ao mouse.
import java.awt.event.MouseEvent; //Representa eventos de mouse.
import java.io.ByteArrayInputStream; //Cria fluxo de entrada a partir de um array de bytes.


/**
 *
 * @author gabri
 */
public class FrameInicial extends javax.swing.JFrame {
    private Connection connection;
    public FrameInicial() {
        initComponents();
        initializeDatabaseConnection();
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        jTable1.setRowSorter(sorter);
        
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            // quando click duplo na linha, abre a imagem.
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = jTable1.getSelectedRow();
                    if (selectedRow != -1) {
                        byte[] imageData = (byte[]) jTable1.getValueAt(selectedRow, 3); //dado da imagem na 3 coluna
                        if (imageData != null && imageData.length > 0) {
                            displayImage(imageData);
                        }
                    }
                }
            }
        });
    }
    
    
    // classe interna que representa um objeto de imagem com atributos
    public class Imagem {
    private int id;
    private String nome;
    private String tamanho;
    private byte[] imagemBlob;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public byte[] getImagemBlob() {
        return imagemBlob;
    }

    public void setImagemBlob(byte[] imagemBlob) {
        this.imagemBlob = imagemBlob;
    }
}
    //realiza a conecção com o banco de dados 
    private void initializeDatabaseConnection() {
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            String url = "jdbc:mysql://localhost:3306/speed_sort";
            String usuario = "root";
            String senha = "minha senha";
            connection = DriverManager.getConnection(url, usuario, senha);
            
            carregarDadosDaTabela();
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco de dados.");
        }
    }
    
    // Extrai dados da imagem exibida em um componente imageLabel. 
    // Ele converte a imagem em um formato de array de bytes (byte array).
    private byte[] getImagemData() {
        ImageIcon imageIcon = (ImageIcon) imageLabel.getIcon();

        if (imageIcon != null) {
            Image imagem = imageIcon.getImage();

            BufferedImage bufferedImage = new BufferedImage(imagem.getWidth(null), imagem.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(imagem, 0, 0, null);
            g.dispose();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
                ImageIO.write(bufferedImage, "jpg", baos);
            
                byte[] imageData = baos.toByteArray();
                return imageData;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        JOptionPane.showMessageDialog(this, "Insira uma imagem Primeiro!");
        return null;
    }
    
    //Esta função verifica se uma string contém letras (caracteres alfabéticos). 
    //Ela é usada para garantir que o campo "Tamanho" contenha apenas números inteiros.
    private boolean containsLetter(String text) {
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                return true; 
            }
        }
        return false; 
    }
    
    //Essa função carrega dados do banco de dados e exibe na tabela jTable1. 
    //Ela consulta o banco de dados, obtém os resultados e preenche o modelo da tabela.
    private void carregarDadosDaTabela() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0);

        String sql = "SELECT id, nome, tamanho, imagem_blob FROM imagens";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nome = resultSet.getString("nome");
                String tamanho = resultSet.getString("tamanho");
                byte[] imagemBlob = resultSet.getBytes("imagem_blob");

                Imagem imagem = new Imagem();
                imagem.setId(id);
                imagem.setNome(nome);
                imagem.setTamanho(tamanho);
                imagem.setImagemBlob(imagemBlob);

                modelo.addRow(new Object[]{id, nome, tamanho, imagemBlob});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do banco de dados.");
        }
        jTable1.setDefaultEditor(Object.class, null);
    }


    //Essa função insere novos dados no banco de dados. 
    //Ela recebe o nome, tamanho e uma representação em array de bytes de uma imagem e realiza a inserção no banco de dados. 
    //Após a inserção, a tabela é atualizada para refletir os novos dados.
    private void inserirDadosNoBanco(String nome, String tamanho, byte[] imagem) {
        String sql = "INSERT INTO imagens (nome, tamanho, imagem_blob) VALUES (?, ?, ?)";
        
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, nome);
            preparedStatement.setString(2, tamanho);
            preparedStatement.setBytes(3, imagem);
            
            int resultado = preparedStatement.executeUpdate();
            carregarDadosDaTabela();
            if (resultado == 1) {
                JOptionPane.showMessageDialog(this, "Dados inseridos no banco de dados com sucesso.");
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao inserir dados no banco de dados.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao inserir dados no banco de dados.");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        TextFieldTam = new javax.swing.JTextField();
        UpdateButton = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();
        InsertButton = new javax.swing.JButton();
        imageLabel = new javax.swing.JLabel();
        openImageButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        QuickButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        MergeButton = new javax.swing.JButton();
        HeapButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        timeLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        TextFieldName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        timeText = new javax.swing.JLabel();
        reloadDatabase = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(232, 219, 199));

        jLabel2.setText("Tamanho:");

        TextFieldTam.setColumns(7);
        TextFieldTam.setMaximumSize(new java.awt.Dimension(7, 7));
        TextFieldTam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextFieldTamActionPerformed(evt);
            }
        });
        TextFieldTam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TextFieldTamKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                TextFieldTamKeyTyped(evt);
            }
        });

        UpdateButton.setBackground(new java.awt.Color(59, 102, 3));
        UpdateButton.setForeground(new java.awt.Color(255, 255, 255));
        UpdateButton.setText("Editar");
        UpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateButtonActionPerformed(evt);
            }
        });

        DeleteButton.setBackground(new java.awt.Color(59, 102, 3));
        DeleteButton.setForeground(new java.awt.Color(255, 255, 255));
        DeleteButton.setText("Excluir");
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonActionPerformed(evt);
            }
        });

        InsertButton.setBackground(new java.awt.Color(59, 102, 3));
        InsertButton.setForeground(new java.awt.Color(255, 255, 255));
        InsertButton.setText("Cadastrar");
        InsertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertButtonActionPerformed(evt);
            }
        });

        openImageButton.setBackground(new java.awt.Color(59, 102, 3));
        openImageButton.setForeground(new java.awt.Color(255, 255, 255));
        openImageButton.setText("Selecionar Imagem");
        openImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openImageButtonActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nome", "Tamanho", "blob"
            }
        ));
        jTable1.setGridColor(new java.awt.Color(59, 102, 3));
        jTable1.setSelectionBackground(new java.awt.Color(59, 102, 3));
        jTable1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        QuickButton.setBackground(new java.awt.Color(59, 102, 3));
        QuickButton.setForeground(new java.awt.Color(255, 255, 255));
        QuickButton.setText("Quick Sort");
        QuickButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QuickButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(59, 102, 3));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Ordenações");

        MergeButton.setBackground(new java.awt.Color(59, 102, 3));
        MergeButton.setForeground(new java.awt.Color(255, 255, 255));
        MergeButton.setText("Merge Sort");
        MergeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MergeButtonActionPerformed(evt);
            }
        });

        HeapButton.setBackground(new java.awt.Color(59, 102, 3));
        HeapButton.setForeground(new java.awt.Color(255, 255, 255));
        HeapButton.setText("Heap Sort");
        HeapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HeapButtonActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(59, 102, 3));
        jLabel4.setText("CADASTRE, EDITE OU EXCLUA IMAGENS");

        timeLabel.setText(" ");

        jLabel1.setText("Nome");

        timeText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        reloadDatabase.setBackground(new java.awt.Color(59, 102, 3));
        reloadDatabase.setForeground(new java.awt.Color(255, 255, 255));
        reloadDatabase.setText("Recarregar Banco");
        reloadDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadDatabaseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(InsertButton)
                                .addGap(18, 18, 18)
                                .addComponent(UpdateButton)
                                .addGap(18, 18, 18)
                                .addComponent(DeleteButton)
                                .addGap(18, 18, 18)
                                .addComponent(openImageButton))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(241, 241, 241)
                                    .addComponent(imageLabel))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGap(9, 9, 9)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel1))
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(TextFieldTam, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .addComponent(TextFieldName))
                                    .addGap(0, 0, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(QuickButton)
                                .addGap(50, 50, 50)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(MergeButton))
                                .addGap(42, 42, 42)
                                .addComponent(HeapButton))
                            .addComponent(timeText, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(115, 115, 115)
                                .addComponent(reloadDatabase)
                                .addGap(26, 26, 26)
                                .addComponent(timeLabel)))))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(imageLabel)
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(TextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(TextFieldTam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(InsertButton)
                    .addComponent(UpdateButton)
                    .addComponent(DeleteButton)
                    .addComponent(openImageButton))
                .addGap(29, 29, 29)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(QuickButton)
                    .addComponent(MergeButton)
                    .addComponent(HeapButton))
                .addGap(27, 27, 27)
                .addComponent(timeText)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeLabel)
                    .addComponent(reloadDatabase))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    /**
     *  Esta função é acionada quando o botão "Editar" na interface é pressionado.
     *  Verifica se uma linha na tabela está selecionada e obtém o ID do registro selecionado.
     *  Obtém os novos valores para o nome e tamanho do registro a ser atualizado.
     *  Realiza verificações nos campos para garantir que o tamanho não contenha letras e que nenhum campo esteja vazio.
     *  Chama a função atualizarRegistroNoBanco para atualizar o registro no banco de dados.
     *  Atualiza a tabela com os novos valores se a atualização for bem-sucedida.
     */
    
    private void UpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        if (jTable1.getSelectedRow() != -1) {
            int selectedRow = jTable1.getSelectedRow();
            int id = (int) model.getValueAt(selectedRow, 0);
            String novoNome = TextFieldName.getText();
            String novoTamanho = TextFieldTam.getText();
            
            if (containsLetter(novoTamanho)) {
                JOptionPane.showMessageDialog(null, "A entrada contém letras. Digite somente números inteiros.");
            } else {
                if(novoNome.length() == 0 || novoTamanho.length() == 0){
                    JOptionPane.showMessageDialog(null, "Por Favor, não deixe campos vazios");  
                }else{
                    if(novoTamanho.length() > 7 || novoTamanho.length() == 0){
                        JOptionPane.showMessageDialog(null, "Por Favor, insira um tamanho com no max 7 caracteres");     
                    } else{

                        if (atualizarRegistroNoBanco(id, novoNome, novoTamanho)) {
                            model.setValueAt(novoTamanho, selectedRow, 2); // Suponhamos que a coluna 2 seja a coluna "Tamanho" na tabela
                            JOptionPane.showMessageDialog(null, "Registro atualizado com sucesso.");
                            carregarDadosDaTabela();
                        } else {
                            JOptionPane.showMessageDialog(null, "Erro ao atualizar o registro no banco de dados.");
                        }
                    }
                }
            }
        }
    }                                            

    /**
     * Esta função é acionada quando o botão "Excluir" na interface é pressionado.
     * Verifica se uma linha na tabela está selecionada e obtém o ID do registro selecionado.
     * Chama a função excluirRegistroDoBanco para excluir o registro do banco de dados.
     * Remove a linha da tabela se a exclusão for bem-sucedida. 
     */
    
    private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        if (jTable1.getSelectedRow() != -1) {
            int selectedRow = jTable1.getSelectedRow();
            int id = (int) model.getValueAt(selectedRow, 0);

            if (excluirRegistroDoBanco(id)) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(null, "Registro excluído com sucesso.");
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao excluir o registro no banco de dados.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Selecione uma linha para ser excluída.");
        } carregarDadosDaTabela();
    }                                            

    /**
     * Esta função atualiza um registro no banco de dados com os novos valores de nome e tamanho.
     * Executa uma consulta SQL para atualizar o registro com base no ID.
     */ 
    
    private boolean atualizarRegistroNoBanco(int id, String novoNome, String novoTamanho) {
        String sql = "UPDATE imagens SET nome = ?, tamanho = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, novoNome);
            statement.setString(2, novoTamanho);
            statement.setInt(3, id);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Esta função exclui um registro do banco de dados com base no ID fornecido.
     * Executa uma consulta SQL de exclusão para remover o registro.
     */

    private boolean excluirRegistroDoBanco(int id) {
        String sql = "DELETE FROM imagens WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Esta função é responsável por exibir uma imagem na interface gráfica.
     * Carrega uma imagem a partir de um array de bytes e a exibe em um rótulo na interface.
     */
     
    private void displayImage(byte[] imageData) {
        try {
            if (imageData != null && imageData.length > 0) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                ImageIcon imageIcon = new ImageIcon(img);

                // Exibe a imagem em um rótulo ou em qualquer outro componente Swing
                JLabel imageLabel = new JLabel(imageIcon);

                // Crie um diálogo para exibir a imagem
                JDialog dialog = new JDialog();
                dialog.add(new JScrollPane(imageLabel));
                dialog.pack();
                dialog.setVisible(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Esta função é acionada quando o botão "Selecionar Imagem" na interface é pressionado.
     * Abre uma caixa de diálogo para o usuário selecionar um arquivo de imagem.
     * Carrega a imagem selecionada e a redimensiona para exibição na interface.
     */
    
    private void openImageButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ImageIcon originalImageIcon = new ImageIcon(selectedFile.getAbsolutePath());
                Image originalImage = originalImageIcon.getImage();

                Image scaledImage = originalImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                imageLabel.getParent().setLayout(null);

                int x = 310;
                int y = 85;
                int width = 100;
                int height = 100;
                imageLabel.setBounds(x, y, width, height);

                imageLabel.setIcon(scaledImageIcon);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }                                               
   
    /**
     * Esta função é acionada quando o botão "Cadastrar" na interface é pressionado.
     * Obtém o nome, tamanho e dados da imagem a ser inserida no banco de dados.    
     * Realiza verificações nos campos para garantir que o tamanho não contenha letras e que nenhum campo esteja vazio.
     * Chama a função inserirDadosNoBanco para inserir o novo registro no banco de dados.
     * Adiciona os valores à tabela e limpa os campos de entrada.
     */
    
    private void InsertButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        String nome = TextFieldName.getText(); 
        String tamanho = TextFieldTam.getText();
        byte[] imagem_blob = getImagemData();

        if (containsLetter(tamanho)) {
            JOptionPane.showMessageDialog(null, "A entrada contém letras. Digite somente números inteiros.");
        } else {
            if(nome.length() == 0 || tamanho.length() == 0){
                JOptionPane.showMessageDialog(null, "Por Favor, não deixe campos vazios");  
            }else{
                if(tamanho.length() > 7){
                    JOptionPane.showMessageDialog(null, "Por Favor, insira um tamanho com no max 7 caracteres");     
                } else{
                    inserirDadosNoBanco(nome, tamanho, imagem_blob);

                    DefaultTableModel dtmClientes = (DefaultTableModel) jTable1.getModel();
                    Object[] dados = { nome, tamanho };
                    dtmClientes.addRow(dados);

                    // Limpe os JTextFields
                    TextFieldName.setText("");
                    TextFieldTam.setText("");

                    carregarDadosDaTabela();
                }
            }   
        }
    }                                            

    /**
     * Esta função é acionada quando o usuário clica em uma linha da tabela.
     * Obtém os valores da linha selecionada e preenche os campos de entrada TextFieldName e TextFieldTam com esses valores.
     */
    
    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {                                     
        if (jTable1.getSelectedRow()!= -1){
            TextFieldName.setText(jTable1.getValueAt(jTable1.getSelectedRow(), 1).toString());
            TextFieldTam.setText(jTable1.getValueAt(jTable1.getSelectedRow(), 2).toString());
        }                                        
        else{
            JOptionPane.showMessageDialog(null, "Selecione uma linha para ser alterada.");           
        }       
    }                                    

    private void TextFieldTamActionPerformed(java.awt.event.ActionEvent evt) {                                             
        // TODO add your handling code here:
    }                                            

    // MergeSort
    // Dicionario
    /**
     * O MergeSort é um algoritmo de ordenação baseado na estratégia "dividir para conquistar". Ele divide o array em duas metades, ordena cada metade separadamente e depois mescla as duas metades ordenadas para obter o resultado final.
     * A função mergeSort é a função principal que inicia o processo de ordenação.
     * Ela divide o array repetidamente até que cada subarray contenha apenas um elemento e, em seguida, mescla esses subarrays para criar subarrays maiores até que o array inteiro esteja ordenado.
     */
    public void mergeSort(int[] array, int left, int right) {
        if (right <= left) return;
        int mid = (left+right)/2;
        mergeSort(array, left, mid);
        mergeSort(array, mid+1, right);
        merge(array, left, mid, right);
    }

    void merge(int[] array, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int L[] = new int[n1];
        int R[] = new int[n2];

        for (int i=0; i<n1; ++i)
            L[i] = array[left + i];
        for (int j=0; j<n2; ++j)
            R[j] = array[mid + 1+ j];

        int i = 0, j = 0;

        int k = left;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                array[k] = L[i];
                i++;
            } else {
                array[k] = R[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            array[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            array[k] = R[j];
            j++;
            k++;
        }
    }

    // QuickSort
    // Video na aula
    /**
     *  O QuickSort também é um algoritmo de ordenação "dividir para conquistar". Ele escolhe um elemento chamado "pivô" do array e reorganiza os elementos de forma que todos os elementos menores que o pivô estejam à esquerda e todos os elementos maiores estejam à direita.
     * A função quickSort é a função principal que inicia o processo de ordenação.
     * Ela seleciona um pivô e chama a função partition para rearranjar os elementos de forma que o pivô esteja na posição correta e, em seguida, ordena as partições esquerda e direita recursivamente.
     */
    public void quickSort(int[] array, int low, int high) {
       if (low < high) {
           int pi = partition(array, low, high);

           quickSort(array, low, pi-1); 
           quickSort(array, pi+1, high); 
       }
    }

    int partition(int[] array, int low, int high) {
       int pivot = array[high]; 
       int i = (low-1); 
       for (int j=low; j<high; j++) {
           if (array[j] < pivot) {
               i++;
               int temp = array[i];
               array[i] = array[j];
               array[j] = temp;
           }
       }

       int temp = array[i+1];
       array[i+1] = array[high];
       array[high] = temp;

       return i+1;
    }

    //heapSort
    //arvore binaria
    /**
     * O HeapSort é um algoritmo que utiliza uma estrutura de dados chamada "heap" (uma árvore binária especial) para classificar elementos em um array.
     * A função HeapSort começa construindo um heap máximo (uma estrutura de heap em que o pai é maior do que seus filhos) e depois reorganiza o array para classificar os elementos em ordem crescente.
     * A função heapify é usada para manter a propriedade do heap máximo enquanto constrói o heap e reorganiza o array.
     */
    
    public void HeapSort(int[] arr) {
       int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(arr, n, i);
        for (int i=n-1; i>=0; i--) {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            heapify(arr, i, 0);
        }
    }

    void heapify(int[] arr, int n, int i) {
       int largest = i;
       int l = 2*i + 1;
       int r = 2*i + 2; 
       if (l < n && arr[l] > arr[largest])
           largest = l;
       if (r < n && arr[r] > arr[largest])
           largest = r;
       if (largest != i) {
           int swap = arr[i];
           arr[i] = arr[largest];
           arr[largest] = swap;
           heapify(arr, n, largest);
       }
    }

    /** (Valido para as 3 proximas funções)
     * Primeiro, obtém o modelo da tabela de dados existente.
     * Calcula o número de linhas na tabela.
     * Cria um array chamado tamanhos para armazenar os valores a serem ordenados.
     * Itera pelas linhas da tabela para obter os valores de tamanho (coluna 2) e os armazena no array tamanhos.
     * Registra o tempo inicial.
     * Chama a uma função de ordenação para ordenar o array tamanhos.
     * Registra o tempo final.
     * Calcula o tempo total gasto na ordenação e o formata.
     * Cria um novo modelo de tabela chamado newModel.
     * Preenche esse novo modelo com os dados da tabela original, mas na ordem especificada pelos valores ordenados em tamanhos.
     * Define o novo modelo na tabela.
     * Define o texto do componente timeText para mostrar o tempo gasto na ordenação.
     */
    private void QuickButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int rowCount = model.getRowCount();
        int[] tamanhos = new int[rowCount];
        
        
        for (int i = 0; i < rowCount; i++) {
        Object tamanhoObj = model.getValueAt(i, 2);
        if (tamanhoObj != null) {
            String tamanhoStr = tamanhoObj.toString();
            try {
                int tamanho = Integer.parseInt(tamanhoStr);
                tamanhos[i] = tamanho;
            } catch (NumberFormatException e) {
                tamanhos[i] = 0;
            }
        }
    }
        for (int i = 0; i < rowCount; i++) {
            String tamanhoStr = model.getValueAt(i, 2).toString();
            int tamanho;
            try {
                tamanho = Integer.parseInt(tamanhoStr);
            } catch (NumberFormatException e) {
                tamanho = 0;
            }
            tamanhos[i] = tamanho;
        }

        long tempoInicial = System.currentTimeMillis();
        quickSort(tamanhos, 0, rowCount - 1);
        long tempoFinal = System.currentTimeMillis();

        long tempoTotal = tempoFinal - tempoInicial;

        long minutos = (tempoTotal / 60000) % 60;
        long segundos = (tempoTotal / 1000) % 60;
        long milissegundos = tempoTotal % 1000;

        String tempoFormatado = String.format("%02d:%02d:%03d", minutos, segundos, milissegundos);

        
        DefaultTableModel newModel = new DefaultTableModel();

        newModel.addColumn("ID");
        newModel.addColumn("Nome");
        newModel.addColumn("Tamanho");
        newModel.addColumn("Imagem Blob");

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                if (tamanhos[i] == Integer.parseInt(model.getValueAt(j, 2).toString())) {
                    newModel.addRow(new Object[]{model.getValueAt(j, 0), model.getValueAt(j, 1), model.getValueAt(j, 2), model.getValueAt(j, 3)});
                    break;
                }
            }
        }

        jTable1.setModel(newModel);
        
        JOptionPane.showMessageDialog(null,"Tempo gasto na ordenação: " + tempoFormatado);
    }                                           

    private void MergeButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int rowCount = model.getRowCount();

        int[] tamanhos = new int[rowCount];

        for (int i = 0; i < rowCount; i++) {
            String tamanhoStr = model.getValueAt(i, 2).toString();
            int tamanho;
            try {
                tamanho = Integer.parseInt(tamanhoStr);
            } catch (NumberFormatException e) {
                tamanho = 0; 
            }
            tamanhos[i] = tamanho;
        }
        
        long tempoInicial = System.currentTimeMillis();
        mergeSort(tamanhos, 0, rowCount - 1);
        long tempoFinal = System.currentTimeMillis();

        long tempoTotal = tempoFinal - tempoInicial;

        long minutos = (tempoTotal / 60000) % 60;
        long segundos = (tempoTotal / 1000) % 60;
        long milissegundos = tempoTotal % 1000;

        String tempoFormatado = String.format("%02d:%02d:%03d", minutos, segundos, milissegundos);

        
        DefaultTableModel newModel = new DefaultTableModel();

        newModel.addColumn("ID");
        newModel.addColumn("Nome");
        newModel.addColumn("Tamanho");
        newModel.addColumn("Imagem Blob");

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                if (tamanhos[i] == Integer.parseInt(model.getValueAt(j, 2).toString())) {
                    newModel.addRow(new Object[]{model.getValueAt(j, 0), model.getValueAt(j, 1), model.getValueAt(j, 2), model.getValueAt(j, 3)});
                    break;
                }
            }
        }

        jTable1.setModel(newModel);
    JOptionPane.showMessageDialog(null,"Tempo gasto na ordenação: " + tempoFormatado);
    }                                           

    private void HeapButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
         DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int rowCount = model.getRowCount();
        int[] tamanhos = new int[rowCount];

        for (int i = 0; i < rowCount; i++) {
            String tamanhoStr = model.getValueAt(i, 2).toString();
            int tamanho;
            try {
                tamanho = Integer.parseInt(tamanhoStr);
            } catch (NumberFormatException e) {
                tamanho = 0;
            }
            tamanhos[i] = tamanho;
        }
        
        long tempoInicial = System.currentTimeMillis();
        HeapSort(tamanhos);
        long tempoFinal = System.currentTimeMillis();

        long tempoTotal = tempoFinal - tempoInicial;

        long minutos = (tempoTotal / 60000) % 60;
        long segundos = (tempoTotal / 1000) % 60;
        long milissegundos = tempoTotal % 1000;

        String tempoFormatado = String.format("%02d:%02d:%03d", minutos, segundos, milissegundos);

        
        DefaultTableModel newModel = new DefaultTableModel();
        newModel.addColumn("ID");
        newModel.addColumn("Nome");
        newModel.addColumn("Tamanho");
        newModel.addColumn("blob");

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                if (tamanhos[i] == Integer.parseInt(model.getValueAt(j, 2).toString())) {
                    newModel.addRow(new Object[]{model.getValueAt(j, 0), model.getValueAt(j, 1), model.getValueAt(j, 2),  model.getValueAt(j, 3)});
                    break;
                }
            }
        }
        jTable1.setModel(newModel);
        JOptionPane.showMessageDialog(null,"Tempo gasto na ordenação: " + tempoFormatado);
    }                                          

    private void TextFieldTamKeyPressed(java.awt.event.KeyEvent evt) {                                        
        // TODO add your handling code here:
    }                                       

    private void TextFieldTamKeyTyped(java.awt.event.KeyEvent evt) {                                      
        // TODO add your handling code here:
    }                                     

    private void reloadDatabaseActionPerformed(java.awt.event.ActionEvent evt) {                                               
        carregarDadosDaTabela();
    }                                              

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameInicial().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton DeleteButton;
    private javax.swing.JButton HeapButton;
    private javax.swing.JButton InsertButton;
    private javax.swing.JButton MergeButton;
    private javax.swing.JButton QuickButton;
    private javax.swing.JTextField TextFieldName;
    private javax.swing.JTextField TextFieldTam;
    private javax.swing.JButton UpdateButton;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton openImageButton;
    private javax.swing.JButton reloadDatabase;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel timeText;
    // End of variables declaration                   
}
