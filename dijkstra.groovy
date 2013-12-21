// Define Class
class Node {
    def id
    def edges = []
    int cost = Integer.MAX_VALUE // 最小コスト
    Node from      // 最小コスト時はどのノードからきたときか
    boolean done = false

    String toString() {
        "${done ? '*' : ' '}node#${id}${edges}:${fromPath}"
    }

    String getFromPath() {
        (from ? from.fromPath : '') + "/node#${id}(${cost})"
    }

    void calculateAdjacentNodeCost() {
        if (done) return
        println "calculateAdjacentNodeCost(): begin: ${this}"
        edges.each { edge ->
            // 各接続先ノードのコストがこのノード経由の方がコストが低いか？
            def toNode = edge.to
            int costToNodeFromThisNode = cost + edge.cost
            if (toNode.cost > costToNodeFromThisNode) {
                // 最小コストを更新する。
                toNode.cost = costToNodeFromThisNode
                toNode.from = this
                println "calculateAdjacentNodeCost(): updated edege node: ${toNode}"
            }
        }
        done = true

        // 隣接ノードがまだコスト計算していなければ、再帰的にコスト計算する。
        // TODO この順序が間違っていると答えが間違ってしまう。スタートノードから隣接ノード
        edges.each { edge ->
            def toNode = edge.to
            if (!toNode.done) {
                toNode.calculateAdjacentNodeCost()
            }
        }
        println "calculateAdjacentNodeCost(): done: ${this}"
    }
}

class Edge {
    def id
    Node to         // 接続先ノード
    int cost = 0    // 接続先ノードへの必要コスト

    String toString() {
        "(${cost})->node#${to?.id}"
    }
}

// Setup sample data
def nodes = [
    1: new Node(id: 1), // start node
    2: new Node(id: 2),
    3: new Node(id: 3),
    4: new Node(id: 4),
    5: new Node(id: 5),
    6: new Node(id: 6), // goal node
]

nodes[1].edges = [
    new Edge(cost: 5, to: nodes[2]),
    new Edge(cost: 4, to: nodes[3]),
    new Edge(cost: 2, to: nodes[4]),
]
nodes[2].edges = [
    new Edge(cost: 5, to: nodes[1]),
    new Edge(cost: 2, to: nodes[3]),
    new Edge(cost: 6, to: nodes[6]),
]
nodes[3].edges = [
    new Edge(cost: 4, to: nodes[1]),
    new Edge(cost: 2, to: nodes[2]),
    new Edge(cost: 3, to: nodes[4]),
    new Edge(cost: 2, to: nodes[5]),
]
nodes[4].edges = [
    new Edge(cost: 2, to: nodes[1]),
    new Edge(cost: 3, to: nodes[3]),
    new Edge(cost: 6, to: nodes[5]),
]
nodes[5].edges = [
    new Edge(cost: 2, to: nodes[3]),
    new Edge(cost: 6, to: nodes[4]),
    new Edge(cost: 4, to: nodes[6]),
]
nodes[6].edges = [
    new Edge(cost: 6, to: nodes[2]),
    new Edge(cost: 4, to: nodes[5]),
]

// Solving

def dumpNodes = { label = '' ->
    println "${label}" + "-"*10
    nodes.each { println it }
}

// スタートとゴールの指定
def startNode = nodes[1]
def goalNode = nodes[6]

dumpNodes("Initial")

// スタートノードのコストは0
startNode.cost = 0

dumpNodes()

// スタートノードから再帰的にコスト計算する
startNode.calculateAdjacentNodeCost()

dumpNodes("Done")

// スタートからゴールまでの道を表示する



