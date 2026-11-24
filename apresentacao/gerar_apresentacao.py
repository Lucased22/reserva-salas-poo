# -*- coding: utf-8 -*-
"""
Gera a apresentacao do TP1 a partir de um unico conteudo:
  - TP1-Apresentacao.pptx  (editavel, PowerPoint)
  - build/TP1-Apresentacao.html (convertido em PDF pelo Edge, ver README desta pasta)

Uso:
  python gerar_apresentacao.py

Gerar o PDF a partir do HTML (Microsoft Edge, sem instalar nada extra):
  msedge --headless --disable-gpu --no-pdf-header-footer --print-to-pdf=TP1-Apresentacao.pdf build/TP1-Apresentacao.html
"""
import html
import os

from PIL import Image
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.util import Inches, Pt

BASE = os.path.dirname(os.path.abspath(__file__))
CAP = os.path.join(BASE, "capturas")
BUILD = os.path.join(BASE, "build")

NAVY = "1F3A5F"
TEAL = "2A9D8F"
LIGHT = "F4F6F8"
TEXTO = "1F2937"
CINZA = "6B7280"
BRANCO = "FFFFFF"
FONTE = "Segoe UI"

# ---------------------------------------------------------------------------
# Conteudo (fonte unica para PPTX e HTML)
# ---------------------------------------------------------------------------
SLIDES = [
    {
        "tipo": "capa",
        "titulo": "Sistema de Reserva de Salas",
        "subtitulo": "Trabalho Prático I · Programação Orientada a Objetos",
        "rodape": "Instituto de Computação (IComp) · Universidade Federal do Amazonas · Novembro de 2026",
    },
    {
        "tipo": "lista",
        "titulo": "Objetivo e requisitos",
        "itens": [
            "Coordenar o uso das salas comuns do IComp: salas de reuniões, seminários e laboratórios de graduação.",
            "Impedir que duas reservas ocupem a mesma sala no mesmo horário.",
            "Requisitos mínimos: cadastro, remoção, alteração e consulta de salas e de usuários; reserva e consulta de salas; relatórios para as portas.",
            "Requisitos obrigatórios: interface gráfica (Swing) e banco de dados (SQLite).",
            "Itens extras: validações de dados, remoções protegidas, busca e ordenação nas tabelas.",
        ],
    },
    {
        "tipo": "arquitetura",
        "titulo": "Arquitetura em camadas",
        "caixas": [
            ("Interface", "Swing · abas e painéis"),
            ("Serviço", "ReservaService · regras"),
            ("DAO", "JDBC · PreparedStatement"),
            ("Banco", "SQLite · salas.db"),
        ],
        "itens": [
            "Interface: abas Salas, Usuários, Reservas e Relatórios, com dados recarregados ao trocar de aba.",
            "Serviço: verifica conflitos de horário e impede remover salas ou usuários que tenham reservas.",
            "DAO: acesso às tabelas com consultas parametrizadas, sem concatenar valores no SQL.",
            "Modelo: classes Sala, Usuario e Reserva validam seus dados já no construtor.",
        ],
    },
    {
        "tipo": "dados",
        "titulo": "Modelo de dados",
        "tabelas": [
            ("salas", ["id (PK)", "nome", "tipo", "capacidade"]),
            ("reservas", ["id (PK)", "sala_id (FK)", "usuario_id (FK)", "data", "hora_inicio", "hora_fim", "finalidade"]),
            ("usuarios", ["id (PK)", "nome", "tipo", "email"]),
        ],
        "itens": [
            "Cada reserva pertence a uma sala e a um usuário; uma sala ou usuário pode ter várias reservas.",
            "Conflito: mesma sala, mesma data e intervalos de horário que se cruzam (início antes do fim do outro e fim depois do início do outro).",
            "Horários adjacentes, como 10:00-11:00 depois de 08:00-10:00, são aceitos.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Janela principal e cadastro de salas",
        "imagens": [("01-aba-salas.png", "Aba Salas: formulário e tabela de salas cadastradas")],
        "itens": [
            "Cabeçalho com o nome do sistema e abas para cada área de trabalho.",
            "Tabela com ID, nome, tipo e capacidade, ordenável por qualquer coluna.",
            "Botões Salvar, Novo / Limpar e Remover selecionada.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Edição de salas",
        "imagens": [("02-salas-edicao.png", "Clique em uma sala para carregar seus dados no formulário")],
        "itens": [
            "Ao selecionar uma linha, o formulário é preenchido para alteração.",
            "Nome obrigatório, tipo escolhido em lista e capacidade mínima de 1 pessoa.",
            "Novo / Limpar prepara o formulário para cadastrar outra sala.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Consulta de salas por nome",
        "imagens": [("03-salas-busca.png", "Busca por \"lab\": a tabela mostra só as salas que contêm o texto")],
        "itens": [
            "A busca é feita enquanto o usuário digita.",
            "Não diferencia letras maiúsculas e minúsculas.",
            "Lab 1 e Lab 2 aparecem; as demais salas ficam ocultas.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Regra de remoção protegida",
        "imagens": [
            ("04-confirmacao-remocao-sala.png", "1. Confirmação antes de remover"),
            ("05-erro-remocao-sala.png", "2. Sala com reservas não é removida"),
        ],
        "itens": [
            "A remoção pede confirmação antes de qualquer alteração.",
            "Se a sala tiver reservas, o sistema recusa a remoção e orienta a cancelar as reservas antes.",
            "A mesma regra vale para usuários com reservas.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Cadastro de usuários",
        "imagens": [("06-aba-usuarios.png", "Aba Usuários: professores e funcionários cadastrados")],
        "itens": [
            "Cadastro de professores e funcionários com nome, tipo e e-mail.",
            "O e-mail precisa conter \"@\", ou o cadastro é recusado com mensagem clara.",
            "Busca por nome e tabela ordenável, como na aba de salas.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Nova reserva",
        "imagens": [("07-aba-reservas.png", "Formulário de reserva com sala, responsável, data, horários e finalidade")],
        "itens": [
            "Campos: sala, responsável, data (dd/mm/aaaa), início e término (HH:mm) e finalidade.",
            "Datas e horários inválidos são recusados com mensagem indicando o campo.",
            "A tabela abaixo mostra todas as reservas, ordenadas por data e horário.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Reserva confirmada e conflito de horário",
        "imagens": [
            ("08-reserva-confirmada.png", "1. Reserva sem conflito é gravada"),
            ("09-erro-conflito-horario.png", "2. Horário ocupado: o sistema informa quem ocupa a sala"),
        ],
        "itens": [
            "Se o horário se sobrepõe a outra reserva da mesma sala, a reserva é recusada.",
            "A mensagem indica a sala, o responsável e o horário que já estão reservados.",
            "Salas diferentes, datas diferentes ou horários que não se cruzam continuam disponíveis.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Lista de reservas e cancelamento",
        "imagens": [
            ("10-confirmacao-cancelamento.png", "1. Cancelamento pede confirmação"),
            ("11-aba-reservas-lista.png", "2. Lista atualizada após o cancelamento"),
        ],
        "itens": [
            "Selecione uma reserva na tabela e clique em \"Cancelar reserva selecionada\".",
            "A busca na lista procura em qualquer campo: sala, responsável ou finalidade.",
            "Após cancelar, o horário fica livre para uma nova reserva.",
        ],
    },
    {
        "tipo": "tela",
        "titulo": "Relatório para a porta da sala",
        "imagens": [("12-aba-relatorios.png", "Agenda da sala no período escolhido, pronta para impressão")],
        "itens": [
            "Escolha a sala e o período; o relatório lista as reservas com data, horário, responsável e finalidade.",
            "Cabeçalho com o nome, o tipo e a capacidade da sala.",
            "Botões Imprimir e Salvar em arquivo .txt, para afixar na porta.",
        ],
    },
    {
        "tipo": "testes",
        "titulo": "Testes realizados",
        "linhas": [
            ("Banco e cadastro", "6", "Criação das tabelas, inserção com id, atualização e listagem"),
            ("Reservas e conflitos", "8", "Conflitos parciais e totais, horários adjacentes, outra sala ou data"),
            ("Validações", "5", "Fim antes do início, finalidade vazia, e-mail, capacidade e nome"),
            ("Consultas", "4", "Reservas por sala e período, ordenação por horário"),
            ("Remoções e cancelamento", "6", "Bloqueio de remoção com reservas, cancelamento e liberação do horário"),
            ("Relatório da porta", "4", "Conteúdo do relatório, reservas canceladas e sala disponível"),
        ],
        "total": "33 verificações aprovadas, 0 falhas.",
        "itens": [
            "As verificações rodam sobre o banco SQLite real, em uma pasta temporária.",
            "Também foi conferido que a interface inicia sem exceções.",
        ],
    },
    {
        "tipo": "lista",
        "titulo": "Conclusão",
        "itens": [
            "O sistema atende aos requisitos mínimos e aos obrigatórios (GUI e banco de dados).",
            "Regras de negócio centralizadas no serviço de reservas, com testes automatizados.",
            "Código-fonte público: github.com/Lucased22/reserva-salas-poo",
            "Documentação técnica em Javadoc, entregue junto com esta apresentação.",
        ],
    },
]

RODAPE = "Reserva de Salas · IComp · TP1 POO"


def cor(hexa):
    return RGBColor.from_string(hexa)


# ---------------------------------------------------------------------------
# Renderizacao PPTX
# ---------------------------------------------------------------------------
def pptx_retangulo(s, x, y, w, h, hexa, forma=MSO_SHAPE.RECTANGLE):
    sh = s.shapes.add_shape(forma, Inches(x), Inches(y), Inches(w), Inches(h))
    sh.fill.solid()
    sh.fill.fore_color.rgb = cor(hexa)
    sh.line.fill.background()
    return sh


def pptx_texto(s, x, y, w, h, texto, tam=16, hexa=TEXTO, negrito=False,
               alinh=PP_ALIGN.LEFT, ancora=MSO_ANCHOR.TOP):
    tb = s.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = ancora
    for k, linha in enumerate(texto.split("\n")):
        p = tf.paragraphs[0] if k == 0 else tf.add_paragraph()
        p.alignment = alinh
        r = p.add_run()
        r.text = linha
        r.font.size = Pt(tam)
        r.font.bold = negrito
        r.font.name = FONTE
        r.font.color.rgb = cor(hexa)
    return tb


def pptx_lista(s, x, y, w, h, itens, tam=18, espaco=12):
    tb = s.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    for k, item in enumerate(itens):
        p = tf.paragraphs[0] if k == 0 else tf.add_paragraph()
        p.space_after = Pt(espaco)
        r = p.add_run()
        r.text = "•  " + item
        r.font.size = Pt(tam)
        r.font.name = FONTE
        r.font.color.rgb = cor(TEXTO)
    return tb


def pptx_cabecalho(s, titulo, numero, total):
    pptx_texto(s, 0.6, 0.35, 12.1, 0.8, titulo, tam=30, hexa=NAVY, negrito=True)
    pptx_retangulo(s, 0.6, 1.18, 1.4, 0.06, TEAL)
    pptx_texto(s, 0.6, 6.95, 9.0, 0.35, RODAPE, tam=10, hexa=CINZA)
    pptx_texto(s, 11.7, 6.95, 1.0, 0.35, f"{numero}/{total}", tam=10, hexa=CINZA, alinh=PP_ALIGN.RIGHT)


def pptx_encaixar_imagem(s, caminho, x, y, w, h):
    """Insere a imagem mantendo a proporcao dentro da caixa (x, y, w, h)."""
    iw, ih = Image.open(caminho).size
    escala = min(w / iw, h / ih)
    lw, lh = iw * escala, ih * escala
    return s.shapes.add_picture(caminho, Inches(x + (w - lw) / 2), Inches(y), Inches(lw), Inches(lh))


def pptx_capa(s, d):
    pptx_retangulo(s, 0, 0, 13.333, 7.5, NAVY)
    pptx_texto(s, 0.9, 2.3, 11.5, 1.2, d["titulo"], tam=44, hexa=BRANCO, negrito=True)
    pptx_retangulo(s, 0.9, 3.75, 2.0, 0.08, TEAL)
    pptx_texto(s, 0.9, 4.0, 11.5, 0.6, d["subtitulo"], tam=22, hexa="CFE8E5")
    pptx_texto(s, 0.9, 6.5, 11.5, 0.5, d["rodape"], tam=14, hexa="CFE8E5")


def pptx_lista_slide(s, d):
    pptx_lista(s, 0.6, 1.6, 12.1, 5.2, d["itens"], tam=20, espaco=16)


def pptx_tela(s, d):
    imagens = d["imagens"]
    if len(imagens) == 1:
        arquivo, legenda = imagens[0]
        pptx_encaixar_imagem(s, os.path.join(CAP, arquivo), 0.6, 1.5, 8.0, 4.9)
        pptx_texto(s, 0.6, 6.45, 8.0, 0.4, legenda, tam=12, hexa=CINZA, alinh=PP_ALIGN.CENTER)
    else:
        altura = 2.2
        for k, (arquivo, legenda) in enumerate(imagens):
            y = 1.45 + k * (altura + 0.5)
            pptx_encaixar_imagem(s, os.path.join(CAP, arquivo), 0.6, y, 8.0, altura)
            pptx_texto(s, 0.6, y + altura + 0.02, 8.0, 0.35, legenda, tam=12, hexa=CINZA, alinh=PP_ALIGN.CENTER)
    pptx_retangulo(s, 8.95, 1.6, 0.04, 4.9, "D1D5DB")
    pptx_lista(s, 9.2, 1.6, 3.6, 5.0, d["itens"], tam=15, espaco=12)


def pptx_arquitetura(s, d):
    x = 0.75
    larg, esp = 2.5, 0.6
    for k, (titulo, sub) in enumerate(d["caixas"]):
        caixa = pptx_retangulo(s, x, 1.85, larg, 1.6, NAVY, MSO_SHAPE.ROUNDED_RECTANGLE)
        tf = caixa.text_frame
        tf.word_wrap = True
        tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = tf.paragraphs[0]
        p.alignment = PP_ALIGN.CENTER
        r = p.add_run()
        r.text = titulo
        r.font.size = Pt(20)
        r.font.bold = True
        r.font.name = FONTE
        r.font.color.rgb = cor(BRANCO)
        p2 = tf.add_paragraph()
        p2.alignment = PP_ALIGN.CENTER
        r2 = p2.add_run()
        r2.text = sub
        r2.font.size = Pt(13)
        r2.font.name = FONTE
        r2.font.color.rgb = cor("CFE8E5")
        if k < len(d["caixas"]) - 1:
            seta = pptx_retangulo(s, x + larg + 0.12, 2.42, esp - 0.24, 0.46, TEAL, MSO_SHAPE.RIGHT_ARROW)
        x += larg + esp
    pptx_lista(s, 0.75, 3.95, 11.8, 2.9, d["itens"], tam=16, espaco=10)


def pptx_dados(s, d):
    posicoes = [0.75, 4.85, 8.95]
    # reservas no centro, com as duas tabelas de referencia dos lados
    ordem = [d["tabelas"][0], d["tabelas"][1], d["tabelas"][2]]
    for (nome, campos), x in zip(ordem, posicoes):
        altura = 0.55 + 0.36 * len(campos) + 0.2
        pptx_retangulo(s, x, 1.7, 3.6, 0.55, NAVY)
        pptx_texto(s, x + 0.15, 1.72, 3.3, 0.5, nome, tam=16, hexa=BRANCO, negrito=True, ancora=MSO_ANCHOR.MIDDLE)
        pptx_retangulo(s, x, 2.25, 3.6, altura - 0.55, LIGHT)
        pptx_texto(s, x + 0.2, 2.35, 3.3, altura - 0.6, "\n".join(campos), tam=13, hexa=TEXTO)
    pptx_retangulo(s, 4.4, 2.2, 0.4, 0.45, TEAL, MSO_SHAPE.RIGHT_ARROW)
    pptx_retangulo(s, 8.5, 2.2, 0.4, 0.45, TEAL, MSO_SHAPE.LEFT_ARROW)
    pptx_texto(s, 4.2, 2.7, 0.9, 0.3, "1 : N", tam=11, hexa=CINZA, alinh=PP_ALIGN.CENTER)
    pptx_texto(s, 8.3, 2.7, 0.9, 0.3, "1 : N", tam=11, hexa=CINZA, alinh=PP_ALIGN.CENTER)
    pptx_lista(s, 0.75, 5.0, 11.8, 1.9, d["itens"], tam=15, espaco=8)


def pptx_testes(s, d):
    linhas = [("Área", "Verificações", "Cobertura")] + d["linhas"]
    linhas.append(("Total", str(sum(int(l[1]) for l in d["linhas"])), d["total"]))
    tabela = s.shapes.add_table(len(linhas), 3, Inches(0.6), Inches(1.5), Inches(12.1), Inches(3.9)).table
    tabela.columns[0].width = Inches(3.4)
    tabela.columns[1].width = Inches(1.9)
    tabela.columns[2].width = Inches(6.8)
    for i, linha in enumerate(linhas):
        for j, valor in enumerate(linha):
            c = tabela.cell(i, j)
            c.text = valor
            c.fill.solid()
            if i == 0:
                c.fill.fore_color.rgb = cor(NAVY)
            elif i == len(linhas) - 1:
                c.fill.fore_color.rgb = cor("D5EFEB")
            else:
                c.fill.fore_color.rgb = cor(LIGHT if i % 2 else BRANCO)
            for p in c.text_frame.paragraphs:
                for r in p.runs:
                    r.font.size = Pt(15)
                    r.font.name = FONTE
                    r.font.bold = i in (0, len(linhas) - 1)
                    r.font.color.rgb = cor(BRANCO if i == 0 else TEXTO)
            c.vertical_anchor = MSO_ANCHOR.MIDDLE
    pptx_lista(s, 0.6, 5.7, 12.1, 1.2, d["itens"], tam=15, espaco=6)


def gerar_pptx(caminho):
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    layout_vazio = prs.slide_layouts[6]
    total = len(SLIDES)
    for numero, d in enumerate(SLIDES, 1):
        s = prs.slides.add_slide(layout_vazio)
        if d["tipo"] == "capa":
            pptx_capa(s, d)
            continue
        pptx_cabecalho(s, d["titulo"], numero, total)
        if d["tipo"] == "lista":
            pptx_lista_slide(s, d)
        elif d["tipo"] == "arquitetura":
            pptx_arquitetura(s, d)
        elif d["tipo"] == "dados":
            pptx_dados(s, d)
        elif d["tipo"] == "tela":
            pptx_tela(s, d)
        elif d["tipo"] == "testes":
            pptx_testes(s, d)
    prs.save(caminho)


# ---------------------------------------------------------------------------
# Renderizacao HTML (para PDF via Edge headless)
# ---------------------------------------------------------------------------
CSS = f"""
@page {{ size: 1280px 720px; margin: 0; }}
* {{ box-sizing: border-box; }}
body {{ margin: 0; font-family: "{FONTE}", Arial, sans-serif; color: #{TEXTO}; }}
.slide {{ width: 1280px; height: 720px; position: relative; overflow: hidden;
          padding: 40px 56px 0 56px; page-break-after: always; background: #fff; }}
.slide:last-child {{ page-break-after: auto; }}
h1 {{ color: #{NAVY}; font-size: 36px; margin: 0; font-weight: 700; }}
.acento {{ width: 110px; height: 6px; background: #{TEAL}; margin: 14px 0 30px 0; }}
.rodape {{ position: absolute; left: 56px; right: 56px; bottom: 22px; font-size: 13px; color: #{CINZA};
           display: flex; justify-content: space-between; }}
ul {{ margin: 0; padding-left: 0; list-style: none; }}
li {{ margin-bottom: 14px; font-size: 20px; line-height: 1.35; }}
li:before {{ content: "•  "; }}
.capa {{ background: #{NAVY}; color: #fff; padding: 230px 90px 0 90px; }}
.capa h1 {{ color: #fff; font-size: 60px; }}
.capa .acento {{ margin: 30px 0 30px 0; width: 150px; }}
.capa .sub {{ font-size: 28px; color: #CFE8E5; }}
.capa .info {{ position: absolute; left: 90px; bottom: 60px; font-size: 20px; color: #CFE8E5; }}
.arq {{ display: flex; align-items: center; gap: 0; margin-bottom: 36px; }}
.caixa {{ width: 250px; height: 160px; background: #{NAVY}; border-radius: 14px; color: #fff;
          display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; }}
.caixa b {{ font-size: 24px; }}
.caixa span {{ font-size: 16px; color: #CFE8E5; margin-top: 6px; }}
.seta {{ width: 84px; height: 26px; background: #{TEAL}; margin: 0 6px; clip-path: polygon(0 20%, 65% 20%, 65% 0, 100% 50%, 65% 100%, 65% 80%, 0 80%); }}
.tabs {{ display: flex; gap: 40px; margin-bottom: 30px; }}
.tab {{ width: 360px; }}
.tab .cab {{ background: #{NAVY}; color: #fff; font-weight: 700; font-size: 20px; padding: 12px 16px; }}
.tab .corpo {{ background: #{LIGHT}; padding: 14px 16px; font-size: 18px; line-height: 1.6; }}
.tela {{ display: flex; gap: 40px; height: 520px; }}
.imgs {{ width: 780px; display: flex; flex-direction: column; justify-content: space-around; }}
.imgs figure {{ margin: 0; text-align: center; height: 100%; display: flex; flex-direction: column; justify-content: center; }}
.imgs img {{ max-width: 100%; max-height: 100%; object-fit: contain; align-self: center; }}
.imgs figcaption {{ font-size: 15px; color: #{CINZA}; margin-top: 6px; }}
.lateral {{ width: 400px; border-left: 2px solid #D1D5DB; padding-left: 30px; }}
.lateral li {{ font-size: 18px; }}
table {{ border-collapse: collapse; width: 100%; font-size: 19px; }}
th, td {{ padding: 12px 16px; text-align: left; }}
th {{ background: #{NAVY}; color: #fff; }}
tr:nth-child(even) td {{ background: #{LIGHT}; }}
tr.total td {{ background: #D5EFEB; font-weight: 700; }}
.testes-notas {{ margin-top: 24px; }}
.testes-notas li {{ font-size: 18px; }}
"""


def e(texto):
    return html.escape(texto, quote=True)


def html_slide(d, numero, total):
    rodape = f'<div class="rodape"><span>{e(RODAPE)}</span><span>{numero}/{total}</span></div>'
    cabecalho = f'<h1>{e(d["titulo"])}</h1><div class="acento"></div>'
    if d["tipo"] == "capa":
        return (f'<section class="slide capa"><h1>{e(d["titulo"])}</h1><div class="acento"></div>'
                f'<div class="sub">{e(d["subtitulo"])}</div>'
                f'<div class="info">{e(d["rodape"])}</div></section>')
    corpo = ""
    if d["tipo"] == "lista":
        itens = "".join(f"<li>{e(i)}</li>" for i in d["itens"])
        corpo = f"<ul>{itens}</ul>"
    elif d["tipo"] == "arquitetura":
        partes = []
        for k, (titulo, sub) in enumerate(d["caixas"]):
            partes.append(f'<div class="caixa"><b>{e(titulo)}</b><span>{e(sub)}</span></div>')
            if k < len(d["caixas"]) - 1:
                partes.append('<div class="seta"></div>')
        itens = "".join(f"<li>{e(i)}</li>" for i in d["itens"])
        corpo = f'<div class="arq">{"".join(partes)}</div><ul>{itens}</ul>'
    elif d["tipo"] == "dados":
        blocos = []
        for nome, campos in d["tabelas"]:
            lista = "".join(f"<div>{e(c)}</div>" for c in campos)
            blocos.append(f'<div class="tab"><div class="cab">{e(nome)}</div>'
                          f'<div class="corpo">{lista}</div></div>')
        itens = "".join(f"<li>{e(i)}</li>" for i in d["itens"])
        corpo = f'<div class="tabs">{"".join(blocos)}</div><ul>{itens}</ul>'
    elif d["tipo"] == "tela":
        figuras = []
        for arquivo, legenda in d["imagens"]:
            src = "../capturas/" + arquivo  # HTML fica em build/
            figuras.append(f'<figure><img src="{e(src)}"><figcaption>{e(legenda)}</figcaption></figure>')
        itens = "".join(f"<li>{e(i)}</li>" for i in d["itens"])
        corpo = (f'<div class="tela"><div class="imgs">{"".join(figuras)}</div>'
                 f'<div class="lateral"><ul>{itens}</ul></div></div>')
    elif d["tipo"] == "testes":
        linhas = "".join(f"<tr><td>{e(a)}</td><td>{e(n)}</td><td>{e(c)}</td></tr>" for a, n, c in d["linhas"])
        total_n = sum(int(l[1]) for l in d["linhas"])
        linhas += f'<tr class="total"><td>Total</td><td>{total_n}</td><td>{e(d["total"])}</td></tr>'
        itens = "".join(f"<li>{e(i)}</li>" for i in d["itens"])
        corpo = (f'<table><tr><th>Área</th><th>Verificações</th><th>Cobertura</th></tr>{linhas}</table>'
                 f'<ul class="testes-notas">{itens}</ul>')
    return f'<section class="slide">{cabecalho}{corpo}{rodape}</section>'


def gerar_html(caminho):
    total = len(SLIDES)
    paginas = "".join(html_slide(d, n, total) for n, d in enumerate(SLIDES, 1))
    documento = (f'<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">'
                 f'<title>TP1 - Apresentacao</title><style>{CSS}</style></head>'
                 f'<body>{paginas}</body></html>')
    os.makedirs(os.path.dirname(caminho), exist_ok=True)
    with open(caminho, "w", encoding="utf-8") as f:
        f.write(documento)


if __name__ == "__main__":
    pptx_path = os.path.join(BASE, "TP1-Apresentacao.pptx")
    html_path = os.path.join(BUILD, "TP1-Apresentacao.html")
    gerar_pptx(pptx_path)
    gerar_html(html_path)
    print("PPTX:", pptx_path)
    print("HTML:", html_path)
