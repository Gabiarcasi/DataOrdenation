import os
import random
import mysql.connector
from PIL import Image
import time

# Gerar um número aleatório exclusivo pro tamanho
def generate_unique_random(existing_values):
    while True:
        random_num = random.randint(1, 10000000)
        if random_num not in existing_values:
            existing_values.add(random_num)
            return random_num

# Redimensiona e comprimi a imagem antes de colocar no banco
def redimensionar_comprimir_imagem(imagem, largura, altura):
    imagem.thumbnail((largura, altura), Image.ANTIALIAS)
    return imagem

# Conecção com o banco de dados
connection = mysql.connector.connect(
    host='localhost',
    database='speed_sort',
    user='root',
    password='Gabrielaacds.2004'
)
cursor = connection.cursor()

pasta_imagens = 'D:/Outros/faculdade/APS/converter/imgs'

# Tamanho pra redimensionar as imagens
largura_desejada = 800
altura_desejada = 600

# Lista para rastrear números aleatórios já usados
numeros_aleatorios_usados = set()

contador = 0

# Loop pelas imagens na pasta
for _ in range(300):
    for filename in os.listdir(pasta_imagens):
        if filename.endswith(('.jpg', '.png', '.jpeg')): 
            with Image.open(os.path.join(pasta_imagens, filename)) as img:
                # Redimensiona a imagem, mantendo a proporção
                max_size = (200, 200)
                img.thumbnail(max_size, Image.LANCZOS)
                
                # Salvar a imagem otimizada em um buffer
                from io import BytesIO
                buffer = BytesIO()
                img.save(buffer, format="JPEG", quality=85)
                image_binary = buffer.getvalue()

            # Gere um número aleatório exclusivo
            tamanho_aleatorio = generate_unique_random(numeros_aleatorios_usados)

            # Insira a imagem no banco de dados
            cursor.execute("INSERT INTO imagens (nome, imagem_blob, tamanho) VALUES (%s, %s, %s)",
                        (filename, image_binary, tamanho_aleatorio))
            connection.commit()
# Feche a conexão com o banco de dados
cursor.close()
connection.close()
