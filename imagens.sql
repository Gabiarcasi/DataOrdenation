-- speed_sort.imagens Criação da tabela

CREATE TABLE `imagens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `tamanho` varchar(100) NOT NULL,
  `imagem_blob` longblob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19880 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;