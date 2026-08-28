# Diagramas e fluxos

Os diagramas abaixo usam Mermaid e são renderizados diretamente pelo GitHub.
Eles permitem acompanhar visualmente a navegação, as dependências e os pontos
críticos da integração.

## Jornada principal

```mermaid
flowchart TD
    A[Home] -->|Vender| B[Catálogo de eventos]
    A -->|Histórico| H[Histórico de vendas]

    B --> C[Selecionar ingressos]
    C --> D[Carrinho validado]
    D -->|Pagar com Cielo| E[Persistir tentativa CREATED]
    E --> F[Transição atômica para PROCESSING]
    F --> G[Abrir Cielo Smart]

    G -->|Aprovado| I[Persistir APPROVED]
    G -->|Negado| J[Persistir DENIED]
    G -->|Cancelado| K[Persistir CANCELLED]
    G -->|Falha| L[Persistir ERROR]

    I --> M[Comprovante]
    M --> N[QR Code do ingresso]
    J --> O[Resultado no BottomSheet]
    K --> O
    L --> O

    H --> P[Filtrar por status]
    P --> Q[Selecionar uma compra]
    Q --> M
```

## Arquitetura e dependências

```mermaid
flowchart LR
    subgraph Presentation[Apresentação]
        XML[Layouts XML]
        Fragment[Fragments e adapters]
        ViewModel[ViewModels e UiState]
    end

    subgraph Domain[Domínio]
        UseCase[Contratos de use case]
        UseCaseImpl[Classes de use case Impl]
        Model[Modelos e invariantes]
        Repository[Contratos de repository]
        Gateway[Contratos de gateway]
    end

    subgraph Infrastructure[Infraestrutura]
        RoomRepo[RoomPurchaseRepositoryImpl]
        Room[(Room)]
        Catalog[LocalEventRepositoryImpl]
        Cielo[CieloPaymentGatewayImpl]
        Intent[Deep Link Cielo]
    end

    XML --> Fragment
    Fragment --> ViewModel
    ViewModel --> UseCase
    UseCaseImpl -. implementa .-> UseCase
    UseCaseImpl --> Model
    UseCaseImpl --> Repository
    UseCaseImpl --> Gateway
    RoomRepo -. implementa .-> Repository
    RoomRepo --> Room
    Catalog -. implementa .-> Repository
    Cielo -. implementa .-> Gateway
    Cielo --> Intent
```

As setas contínuas representam dependências de execução. As setas tracejadas
indicam implementações concretas de contratos.

## Sequência do pagamento

```mermaid
sequenceDiagram
    actor Operador
    participant View as BottomSheet XML
    participant Checkout as CheckoutViewModel
    participant Create as CreatePurchaseAttemptUseCase
    participant Save as SavePurchaseAttemptUseCase
    participant Start as StartPaymentUseCase
    participant DB as Room
    participant Cielo as Cielo Smart
    participant Callback as CieloResponseActivity

    Operador->>View: Toca em Pagar com Cielo
    View->>Checkout: start(cart)
    Checkout->>Create: criar snapshot e UUID
    Create-->>Checkout: PurchaseAttempt CREATED
    Checkout->>Save: persistir tentativa e itens
    Save->>DB: transação
    DB-->>Save: salvo
    Save-->>Checkout: Saved
    Checkout->>Start: iniciar tentativa persistida
    Start->>DB: compare-and-set CREATED → PROCESSING

    alt transição adquirida
        DB-->>Start: Updated
        Start->>Cielo: abrir lio://payment
        Cielo-->>Callback: order://payment?response=Base64
        Callback-->>Checkout: broadcast restrito ao pacote
        Checkout->>DB: PROCESSING → status terminal

        alt pagamento aprovado
            Checkout-->>View: navegar para comprovante
        else negado, cancelado ou erro
            Checkout-->>View: exibir resultado terminal
        end
    else tentativa já processando
        DB-->>Start: StatusMismatch
        Start-->>Checkout: AlreadyProcessing
    end
```

## Máquina de estados

```mermaid
stateDiagram-v2
    [*] --> CREATED: tentativa persistida
    CREATED --> PROCESSING: cobrança reivindicada
    PROCESSING --> APPROVED: pagamento confirmado
    PROCESSING --> DENIED: pagamento negado
    PROCESSING --> CANCELLED: usuário cancelou
    PROCESSING --> ERROR: falha técnica ou autenticação

    APPROVED --> [*]
    DENIED --> [*]
    CANCELLED --> [*]
    ERROR --> [*]
```

Estados terminais não aceitam novas transições. Repetições do mesmo callback são
tratadas como idempotentes.

## Histórico e comprovante

```mermaid
flowchart LR
    DB[(Room)] --> Flow[Flow ordenado por createdAt]
    Flow --> VM[HistoryViewModel]
    Filter[Filtro selecionado] --> VM
    VM --> List[Lista filtrada]
    List -->|Referência| ReceiptVM[ReceiptViewModel]
    ReceiptVM --> Lookup[GetPurchaseAttemptUseCase]
    Lookup --> DB
    ReceiptVM --> Receipt[Comprovante persistido]
    Receipt -->|Somente APPROVED| QR[QR Code opaco]
```

O filtro modifica apenas o estado de apresentação. O comprovante sempre é
recarregado do banco pela referência, evitando dados de navegação desatualizados.
